package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Server that listens to connections and creates communicator threads.
 */
public class Server implements Closeable {
    private final PrintStream reportStream;
    private final Set<ServerCommunicator> users;
    private Supplier<ShellState> shellStateSupplier;
    private ServerSocket serverSocket;
    private boolean deletingFromUsers = false;

    public Server(PrintStream reportStream) {
        this.reportStream = reportStream;
        users = new HashSet<>();
    }

    public synchronized Supplier<ShellState> getShellStateSupplier() {
        return shellStateSupplier;
    }

    public synchronized void setShellStateSupplier(Supplier<ShellState> shellStateSupplier) {
        if (isStarted()) {
            throw new IllegalStateException("Cannot set state supplier: already started");
        }
        this.shellStateSupplier = shellStateSupplier;
    }

    public PrintStream getReportStream() {
        return reportStream;
    }

    public synchronized ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Called by ServerCommunicator after it is closed.
     */
    synchronized void onConnectionClosed(ServerCommunicator communicator) {
        Log.log(Server.class, "Disconnected: " + communicator);
        if (!deletingFromUsers) {
            users.remove(communicator);
        }
    }

    public synchronized boolean isStarted() {
        return serverSocket != null;
    }

    public synchronized void startServer(int port) throws IOException, IllegalStateException {
        if (isStarted()) {
            throw new IllegalStateException("not started: already started");
        }

        if (shellStateSupplier == null) {
            throw new IllegalStateException("not started: state supplier not defined");
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException exc) {
            throw new IOException("not started: " + exc.getMessage(), exc.getCause());
        }

        Thread serverThread = new Thread(this::run, this + ": server thread");
        serverThread.setDaemon(true);
        serverThread.setPriority(Thread.MIN_PRIORITY);
        serverThread.start();
    }

    public synchronized void stopServerIfStarted() throws IOException {
        if (isStarted()) {
            close();
        }
    }

    public synchronized int stopServer() throws IOException, IllegalStateException {
        requireStarted();
        int port = getServerSocket().getLocalPort();
        close();
        return port;
    }

    public synchronized List<ServerCommunicator> listUsers() {
        requireStarted();
        return new LinkedList<>(users);
    }

    private void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ServerSocket serverSocketTemp;

                synchronized (this) {
                    serverSocketTemp = serverSocket;
                }

                if (serverSocketTemp == null) {
                    return;
                }

                Socket socket = serverSocketTemp.accept();
                ServerCommunicator communicator = new ServerCommunicator(socket, this, shellStateSupplier);
                Thread communicatorThread = new Thread(communicator, "Communicator with " + communicator);
                communicatorThread.setDaemon(true);
                communicatorThread.start();

                synchronized (this) {
                    users.add(communicator);
                }
            }
        } catch (IOException exc) {
            Log.log(Server.class, exc, "Error on listening to incoming connections");
        } catch (TerminalException exc) {
            // Already handled.
        } finally {
            try {
                close();
            } catch (IOException exc) {
                Log.log(DBServerState.class, exc, this + ": failed to close server socket");
            }
        }
    }

    private void requireStarted() throws IllegalStateException {
        if (!isStarted()) {
            throw new IllegalStateException("not started");
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (serverSocket != null) {
            deletingFromUsers = true;
            for (ServerCommunicator communicator : users) {
                try {
                    communicator.close();
                } catch (IOException exc) {
                    Log.log(Server.class, exc, "Error on closing socket: " + communicator);
                }
            }
            users.clear();
            deletingFromUsers = false;

            try {
                serverSocket.close();
            } finally {
                serverSocket = null;
            }
        }
    }
}
