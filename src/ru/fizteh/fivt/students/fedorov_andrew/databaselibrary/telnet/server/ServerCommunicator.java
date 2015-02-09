package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.function.Supplier;

public class ServerCommunicator implements Runnable, Closeable {
    private final Socket socket;

    private final Shell interpreter;

    private final PrintStream reportStream;

    private final Server server;

    private boolean closed = false;

    public ServerCommunicator(Socket socket, Server server, Supplier<ShellState> shellStateSupplier)
            throws IOException, TerminalException {
        this.socket = socket;
        this.server = server;
        this.reportStream = server.getReportStream();
        this.interpreter = new Shell<>(shellStateSupplier.get(), socket.getOutputStream());
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try {
            interpreter.run(socket.getInputStream());
        } catch (TerminalException exc) {
            Log.log(ServerCommunicator.class, exc, "Interpreter run terminated");
            // Already handled.
        } catch (IOException exc) {
            reportStream.println(this + ": Error on obtaining socket input stream: " + exc.getMessage());
        } finally {
            try {
                close();
            } catch (IOException exc) {
                Log.log(
                        TelnetDBServerState.class,
                        this + " failed to close connection after all commands execution");
            }

        }
    }

    private void notifyConnectionClosed() {
        server.onConnectionClosed(this);
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            closed = true;
            socket.close();
            notifyConnectionClosed();
        }
    }

    @Override
    public int hashCode() {
        return socket.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerCommunicator) {
            ServerCommunicator user = (ServerCommunicator) obj;
            return socket.equals(user.socket);
        }
        return false;
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
