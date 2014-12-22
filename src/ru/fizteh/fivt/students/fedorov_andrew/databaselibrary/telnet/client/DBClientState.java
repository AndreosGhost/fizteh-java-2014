package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client;

import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote.RemoteDatabaseStorage;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.BaseShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.IOException;
import java.util.Map;

public class DBClientState extends BaseShellState<DBClientState> {
    private final RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
    private RemoteTableProvider remoteProvider;
    private String host;
    private int port = -1;

    public void connect(String host, int port) throws IOException {
        checkInitialized();
        if (remoteProvider == null) {
            try {
                remoteProvider = storage.connect(host, port);
                this.host = host;
                this.port = port;
            } catch (IOException exc) {
                throw new IOException("not connected: " + exc.getMessage(), exc.getCause());
            }
        } else {
            throw new IllegalStateException("not connected: already connected");
        }
    }

    public RemoteTableProvider getRemoteProvider() {
        return remoteProvider;
    }

    public String getHost() {
        requireConnected();
        return host;
    }

    public int getPort() {
        requireConnected();
        return port;
    }

    public void disconnect() throws IllegalStateException, IOException {
        requireConnected();
        try {
            remoteProvider.close();
        } finally {
            remoteProvider = null;
        }
    }

    private void requireConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("not connected");
        }
    }

    public boolean isConnected() {
        return remoteProvider != null;
    }

    @Override
    public void cleanup() {
        try {
            if (isConnected()) {
                disconnect();
            }
        } catch (IOException exc) {
            Log.log(DBClientState.class, exc, "Exception occurred on cleanup");
        }
    }

    @Override
    public Map<String, Command<DBClientState>> getCommands() {
        return ClientCommands.getInstance().getCommands();
    }
}
