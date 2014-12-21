package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.DatabaseIOException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvocationException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.NoActiveTableException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.AbstractCommand;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SimpleCommandContainer;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Map;

public final class ClientCommands extends SimpleCommandContainer<DBClientState> {
    public static final Command<DBClientState> CONNECT = new AbstractCommand<DBClientState>(
            "connect", "<host> <port>", "connects to a database storage server", 3) {
        @Override
        public void executeSafely(DBClientState state, String[] args) throws
                                                                      IllegalArgumentException,
                                                                      NoActiveTableException,
                                                                      IllegalStateException,
                                                                      NullPointerException,
                                                                      InvocationException,
                                                                      ParseException,
                                                                      IOException {
            int port;

            try {
                port = Integer.parseInt(args[2]);
            } catch (NumberFormatException exc) {
                throw new NumberFormatException("not connected: " + exc.getMessage());
            }
            state.connect(args[1], port);
            state.getOutputStream().println("connected");
        }
    };
    public static final Command<DBClientState> DISCONNECT =
            new AbstractCommand<DBClientState>("disconnect", "", "disconnects from database storage", 1) {
                @Override
                public void executeSafely(DBClientState state, String[] args) throws
                                                                              IllegalArgumentException,
                                                                              NoActiveTableException,
                                                                              IllegalStateException,
                                                                              NullPointerException,
                                                                              InvocationException,
                                                                              ParseException,
                                                                              IOException {
                    state.disconnect();
                    state.getOutputStream().println("disconnected");
                }
            };
    public static final Command<DBClientState> WHEREAMI = new AbstractCommand<DBClientState>(
            "whereami",
            "",
            "prints host and port you are connected to",
            1) {
        @Override
        public void executeSafely(DBClientState state, String[] args) throws
                                                                      IllegalArgumentException,
                                                                      NoActiveTableException,
                                                                      IllegalStateException,
                                                                      NullPointerException,
                                                                      InvocationException,
                                                                      ParseException,
                                                                      IOException {
            InetAddress address = state.getHost();
            int port = state.getPort();

            if (address.isLoopbackAddress()) {
                state.getOutputStream().println("local " + port);
            } else {
                state.getOutputStream().println("remote " + address.getHostAddress() + ":" + port);
            }
        }
    };
    public static final Command<DBClientState> HELP = new AbstractCommand<DBClientState>(
            "help", null, "prints out description of state commands", 1, Integer.MAX_VALUE) {
        @Override
        public void execute(DBClientState state, String[] args) {
            Map<String, Command<DBClientState>> commands = state.getCommands();

            state.getOutputStream().println(
                    "You can connect to a database storage server");

            commands.values().forEach((command) -> state.getOutputStream().println(command.buildHelpLine()));
        }

        @Override
        public void executeSafely(DBClientState state, String[] args) throws DatabaseIOException {
            // not used
        }
    };
    public static final Command<DBClientState> EXIT = new AbstractCommand<DBClientState>(
            "exit", null, "saves all data to file system and stops interpretation", 1) {
        @Override
        public void execute(DBClientState state, String[] args) throws TerminalException {
            state.prepareToExit(0);

            // If all contracts are honoured, this line should not be reached.
            throw new AssertionError("Exit request not thrown");
        }

        @Override
        public void executeSafely(DBClientState state, String[] args)
                throws DatabaseIOException, IllegalArgumentException {
            // Not used.
        }
    };
    private static final ClientCommands INSTANCE = new ClientCommands();

    private ClientCommands() {
    }

    public static ClientCommands getInstance() {
        return INSTANCE;
    }
}
