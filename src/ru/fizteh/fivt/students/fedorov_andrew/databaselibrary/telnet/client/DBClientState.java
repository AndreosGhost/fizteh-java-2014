package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExitRequest;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.BaseShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

public class DBClientState extends BaseShellState<DBClientState> {
    private Socket connection;

    public void connect(String host, int port) throws IOException {
        checkInitialized();
        if (connection == null) {
            try {
                connection = new Socket(host, port);
            } catch (IOException exc) {
                throw new IOException("not connected: " + exc.getMessage(), exc.getCause());
            }
        } else {
            throw new IllegalStateException("not connected: already connected");
        }
    }

    public InetAddress getHost() {
        requireConnected();
        return connection.getInetAddress();
    }

    public int getPort() {
        requireConnected();
        return connection.getPort();
    }

    public void disconnect() throws IllegalStateException, IOException {
        requireConnected();
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    private void requireConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("not connected");
        }
    }

    public boolean isConnected() {
        return connection != null;
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
    public void prepareToExit(int exitCode) throws ExitRequest {
        Log.log(DBClientState.class, "Preparing to exit with code:" + exitCode);
        cleanup();
        throw new ExitRequest(exitCode);
    }

    @Override
    public Map<String, Command<DBClientState>> getCommands() {
        return ClientCommands.getInstance().getCommands();
    }
}
