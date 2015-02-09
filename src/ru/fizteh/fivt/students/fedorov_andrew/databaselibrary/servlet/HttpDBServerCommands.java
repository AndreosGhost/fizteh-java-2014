package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.DatabaseIOException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.AbstractCommand;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SimpleCommandContainer;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SingleDatabaseShellState;

import java.util.Map;

/**
 * Container for server commands: start, stop.
 */
public class HttpDBServerCommands extends SimpleCommandContainer<HttpDBServerState> {
    public static final Command<HttpDBServerState> STOPHTTP =
            new AbstractCommand<HttpDBServerState>("stophttp", "", "stops http server", 1) {
                @Override
                public void executeSafely(HttpDBServerState state, String[] args) throws Exception {
                    int port = state.stopHttpServer();
                    state.getOutputStream().println("stopped at " + port);
                }
            };
    public static final Command<HttpDBServerState> EXIT = new AbstractCommand<HttpDBServerState>(
            "exit", "", "stops http server (if it is started) and closes the terminal", 1) {
        @Override
        public void execute(HttpDBServerState state, String[] args) throws TerminalException {
            state.prepareToExit(0);

            // If all contracts are honoured, this line should not be reached.
            throw new AssertionError("Exit request not thrown");
        }

        @Override
        public void executeSafely(HttpDBServerState state, String[] args) throws Exception {
            // Not used.
        }
    };
    public static final Command<HttpDBServerState> HELP = new AbstractCommand<HttpDBServerState>(
            "help", "", "prints out description of state commands", 1, Integer.MAX_VALUE) {
        @Override
        public void execute(HttpDBServerState state, String[] args) {
            Map<String, Command<HttpDBServerState>> commands = state.getCommands();

            state.getOutputStream().println(
                    "You can start http database server ready for new connections!");

            state.getOutputStream().println(
                    String.format(
                            "You can set database directory to work with using environment "
                            + "variable '%s'", SingleDatabaseShellState.DB_DIRECTORY_PROPERTY_NAME));

            for (Command<HttpDBServerState> command : commands.values()) {
                state.getOutputStream().println(command.buildHelpLine());
            }
        }

        @Override
        public void executeSafely(HttpDBServerState state, String[] args) throws DatabaseIOException {
            // not used
        }
    };
    private static final int DEFAULT_PORT = 8080;
    public static final Command<HttpDBServerState> STARTHTTP = new AbstractCommand<HttpDBServerState>(
            "starthttp",
            "[port]",
            "starts http server at the specified port (or, if not specified, at " + DEFAULT_PORT + ")",
            1,
            2) {
        @Override
        public void executeSafely(HttpDBServerState state, String[] args) throws Exception {
            int port;
            if (args.length == 1) {
                port = DEFAULT_PORT;
            } else {
                port = Integer.parseInt(args[1]);
            }

            state.startHttpServer(port);
            state.getOutputStream().println("started at " + port);
        }
    };
    private static final HttpDBServerCommands INSTANCE = new HttpDBServerCommands();

    /**
     * Not for initializing.
     */
    private HttpDBServerCommands() {
    }

    public static HttpDBServerCommands obtainInstance() {
        return INSTANCE;
    }
}
