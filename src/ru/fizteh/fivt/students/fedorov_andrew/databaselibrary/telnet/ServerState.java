package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExitRequest;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.BaseShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ServerState extends BaseShellState<ServerState> {
    private static final ServerCommands COMMANDS = ServerCommands.obtainInstance();
    private final Supplier<ShellState> clientShellStateSupplier;
    private Server server;

    public ServerState(Supplier<ShellState> clientShellStateSupplier) {
        this.clientShellStateSupplier = clientShellStateSupplier;
    }

    public void startServer(int port) throws IOException, IllegalStateException {
        checkInitialized();
        server.startServer(port);
    }

    public int stopServer() throws IOException, NullPointerException {
        checkInitialized();
        return server.stopServer();
    }

    public void stopServerIfStarted() throws IOException {
        checkInitialized();
        server.stopServerIfStarted();
    }

    public int getServerPort() {
        checkInitialized();
        return server.getServerSocket().getLocalPort();
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
        // Nothing to clean.
    }

    @Override
    public void persist() throws Exception {
        // Nothing to persist.
    }

    @Override
    public void prepareToExit(int exitCode) throws ExitRequest {
        try {
            stopServerIfStarted();
        } catch (IOException exc) {
            Log.log(ServerState.class, exc);
            if (exitCode == 0) {
                exitCode = -1;
            }
        }
        throw new ExitRequest(exitCode);
    }

    @Override
    public void init(Shell<ServerState> host) throws Exception {
        super.init(host);
        server = new Server(host.getOutputStream(), clientShellStateSupplier);
    }

    @Override
    public Map<String, Command<ServerState>> getCommands() {
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
