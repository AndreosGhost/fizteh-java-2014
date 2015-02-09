package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.BaseShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.util.Map;

public class HttpDBServerState extends BaseShellState<HttpDBServerState> {
    private final HttpDBServer server;

    private int port = -1;

    public HttpDBServerState(TableProvider localProvider) {
        server = new HttpDBServer(localProvider);
    }

    public void startHttpServer(int port) throws Exception {
        server.startHttpServer("localhost", port);
        this.port = port;
    }

    public int stopHttpServer() throws Exception {
        server.stopHttpServer();
        int oldPort = port;
        port = -1;
        return oldPort;
    }

    public boolean isStarted() {
        return server.isStarted();
    }

    @Override
    public void cleanup() {
        try {
            server.stopHttpServerIfStarted();
        } catch (Exception exc) {
            Log.log(HttpDBServerState.class, exc, "Failed to cleanup");
        }
    }

    @Override
    public Map<String, Command<HttpDBServerState>> getCommands() {
        return HttpDBServerCommands.obtainInstance().getCommands();
    }
}
