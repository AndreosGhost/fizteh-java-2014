package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.DatabaseIOException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvocationException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.NoActiveTableException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.AbstractCommand;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SimpleCommandContainer;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SingleDatabaseShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server.TelnetDBServerState.User;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Container for server commands: start, stop, listusers.
 */
public class ServerCommands extends SimpleCommandContainer<TelnetDBServerState> {
    public static final Command<TelnetDBServerState> STOP =
            new AbstractCommand<TelnetDBServerState>("stop", "", "stops server", 1) {
                @Override
                public void executeSafely(TelnetDBServerState state, String[] args) throws
                                                                                    IllegalArgumentException,
                                                                                    NoActiveTableException,
                                                                                    IllegalStateException,
                                                                                    NullPointerException,
                                                                                    InvocationException,
                                                                                    ParseException,
                                                                                    IOException {
                    int port = state.stopServer();
                    state.getOutputStream().println("stopped at " + port);
                }
            };
    public static final Command<TelnetDBServerState> LISTUSERS = new AbstractCommand<TelnetDBServerState>(
            "listusers", "", "prints list of ip addresses and ports of connected users", 1) {
        @Override
        public void executeSafely(TelnetDBServerState state, String[] args) throws
                                                                            IllegalArgumentException,
                                                                            NoActiveTableException,
                                                                            IllegalStateException,
                                                                            NullPointerException,
                                                                            InvocationException,
                                                                            ParseException,
                                                                            IOException {
            List<User> users = state.listUsers();

            StringBuilder sb = new StringBuilder();
            for (User user : users) {
                sb.append(user);
                sb.append(System.lineSeparator());
            }
            state.getOutputStream().print(sb.toString());
        }
    };
    public static final Command<TelnetDBServerState> EXIT = new AbstractCommand<TelnetDBServerState>(
            "exit", "", "stops server (if it is started) and closes the terminal", 1) {
        @Override
        public void execute(TelnetDBServerState state, String[] args) throws TerminalException {
            state.prepareToExit(0);

            // If all contracts are honoured, this line should not be reached.
            throw new AssertionError("Exit request not thrown");
        }

        @Override
        public void executeSafely(TelnetDBServerState state, String[] args) throws
                                                                            IllegalArgumentException,
                                                                            NoActiveTableException,
                                                                            IllegalStateException,
                                                                            NullPointerException,
                                                                            InvocationException,
                                                                            ParseException,
                                                                            IOException {
            // Not used.
        }
    };
    public static final Command<TelnetDBServerState> HELP = new AbstractCommand<TelnetDBServerState>(
            "help", "", "prints out description of state commands", 1, Integer.MAX_VALUE) {
        @Override
        public void execute(TelnetDBServerState state, String[] args) {
            Map<String, Command<TelnetDBServerState>> commands = state.getCommands();

            state.getOutputStream().println(
                    "You can start telnet database server ready for new connections!");

            state.getOutputStream().println(
                    String.format(
                            "You can set database directory to work with using environment "
                            + "variable '%s'", SingleDatabaseShellState.DB_DIRECTORY_PROPERTY_NAME));

            for (Command<TelnetDBServerState> command : commands.values()) {
                state.getOutputStream().println(command.buildHelpLine());
            }
        }

        @Override
        public void executeSafely(TelnetDBServerState state, String[] args) throws DatabaseIOException {
            // not used
        }
    };
    private static final int DEFAULT_PORT = 10001;
    public static final Command<TelnetDBServerState> START = new AbstractCommand<TelnetDBServerState>(
            "start",
            "[port]",
            "starts server at the specified port (or, if not specified, at " + DEFAULT_PORT + ")",
            1,
            2) {
        @Override
        public void executeSafely(TelnetDBServerState state, String[] args) throws
                                                                            IllegalArgumentException,
                                                                            NoActiveTableException,
                                                                            IllegalStateException,
                                                                            InvocationException,
                                                                            ParseException,
                                                                            IOException {
            int port;
            if (args.length == 1) {
                port = DEFAULT_PORT;
            } else {
                port = Integer.parseInt(args[1]);
            }

            state.startServer(port);
            state.getOutputStream().println("started at " + port);
        }
    };
    private static final ServerCommands INSTANCE = new ServerCommands();

    /**
     * Not for initializing.
     */
    private ServerCommands() {
    }

    public static ServerCommands obtainInstance() {
        return INSTANCE;
    }
}
