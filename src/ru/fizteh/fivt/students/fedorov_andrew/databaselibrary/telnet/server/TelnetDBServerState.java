package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.RemoteTableProviderFactory;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote.RemoteDatabaseStorage;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote.RemoteTableProviderFactoryImpl;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.BaseShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SingleDatabaseShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TelnetDBServerState extends BaseShellState<TelnetDBServerState> {
    private static final ServerCommands COMMANDS = ServerCommands.obtainInstance();
    private final String databaseRoot;
    private final TableProvider localProvider;
    private Supplier<ShellState> clientShellStateSupplier;
    private Server server;
    private RemoteTableProviderFactoryImpl factory;

    public TelnetDBServerState(TableProvider localProvider, String databaseRoot) {
        this.localProvider = localProvider;
        this.databaseRoot = databaseRoot;
        RemoteTableProviderFactory storage = new RemoteDatabaseStorage();
        TelnetDBServerState serverState = this;
        this.clientShellStateSupplier = () -> new SingleDatabaseShellState() {
            @Override
            protected Database obtainNewActiveDatabase() throws Exception {
                RemoteTableProvider provider = storage.connect("127.0.0.1", Registry.REGISTRY_PORT);
                return new Database(
                        provider, serverState.getDatabaseRoot(), getOutputStream());
            }
        };
    }

    public String getDatabaseRoot() {
        return databaseRoot;
    }

    public void startServer(int port) throws IOException, IllegalStateException {
        checkInitialized();
        if (server.isStarted()) {
            throw new IllegalStateException("not started: already started");
        }

        try {
            factory = new RemoteTableProviderFactoryImpl(localProvider);
            factory.establishStorage(databaseRoot);

            server.setShellStateSupplier(clientShellStateSupplier);
            server.startServer(port);
        } catch (Exception exc) {
            try {
                if (factory != null) {
                    factory.close();
                }
            } catch (Exception ignored) {
                Log.log(
                        TelnetDBServerState.class,
                        ignored,
                        "Failed to close factory after server install failure");
            }
            throw exc;
        }
    }

    public boolean isStarted() {
        return server.isStarted();
    }

    private void closeFactory() throws IOException {
        factory.close();
        factory = null;
    }

    public int stopServer() throws IOException {
        checkInitialized();
        int port = server.stopServer();
        closeFactory();
        return port;
    }

    public void stopServerIfStarted() throws IOException {
        checkInitialized();
        server.stopServerIfStarted();
        if (factory != null) {
            closeFactory();
        }
    }

    public List<User> listUsers() {
        checkInitialized();
        return server.listUsers().stream().map(
                (ServerCommunicator communicator) -> new User(
                        communicator.getSocket().getInetAddress(), communicator.getSocket().getPort()))
                     .collect(Collectors.toList());
    }

    @Override
    public void cleanup() {
        try {
            stopServerIfStarted();
        } catch (Exception exc) {
            Log.log(TelnetDBServerState.class, exc);
        }
    }

    @Override
    public void init(Shell<TelnetDBServerState> host) throws Exception {
        super.init(host);
        server = new Server(host.getOutputStream());
    }

    @Override
    public Map<String, Command<TelnetDBServerState>> getCommands() {
        return COMMANDS.getCommands();
    }

    public static class User {
        private final InetAddress address;
        private final int port;

        public User(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return address.getHostAddress() + ":" + port;
        }
    }
}
