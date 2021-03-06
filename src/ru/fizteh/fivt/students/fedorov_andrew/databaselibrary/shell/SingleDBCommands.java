package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.StoreableTableImpl;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.DatabaseIOException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvocationException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.NoActiveTableException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SingleDBCommands extends SimpleCommandContainer<SingleDatabaseShellState> {
    public static final Command<SingleDatabaseShellState> COMMIT =
            new AbstractCommand<SingleDatabaseShellState>(
                    "commit", null, "saves all changes made from the last commit", 1) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws IOException, IllegalArgumentException {
                    int changes = state.getActiveDatabase().commit();
                    state.getOutputStream().println(changes);
                }
            };
    public static final Command<SingleDatabaseShellState> ROLLBACK =
            new AbstractCommand<SingleDatabaseShellState>(
                    "rollback", null, "cancels all changes made from the last commit", 1) {

                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws DatabaseIOException, IllegalArgumentException {
                    int changes = state.getActiveDatabase().rollback();
                    state.getOutputStream().println(changes);
                }
            };
    public static final Command<SingleDatabaseShellState> SIZE =
            new AbstractCommand<SingleDatabaseShellState>(
                    "size", null, "prints count of stored keys in current table", 1) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws DatabaseIOException, IllegalArgumentException, NoActiveTableException {
                    int size = state.getActiveDatabase().getActiveTable().size();
                    state.getOutputStream().println(size);
                }
            };
    public static final Command<SingleDatabaseShellState> CREATE =
            new AbstractCommand<SingleDatabaseShellState>(
                    "create",
                    "<tablename> (type0 type1 ... typeN)",
                    "creates a new table with the given name and column types (must be specified inside "
                    + "round "
                    + "brackets); type can be one of the following: int, long, byte, float, double, "
                    + "boolean, String;",
                    3,
                    Integer.MAX_VALUE) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws IOException, InvocationException {
                    if (!args[2].startsWith("(") || !args[args.length - 1].endsWith(")")) {
                        throw new InvocationException(
                                this,
                                args[0],
                                "Round brackets must exist and contain types list inside them");
                    }

                    // Joining strings.
                    String typesString = String.join(" ", Arrays.asList(args).subList(2, args.length));

                    // Removing brackets.
                    typesString = typesString.substring(1, typesString.length() - 1).trim();

                    List<Class<?>> columnTypes = StoreableTableImpl.parseColumnTypes(typesString);
                    String tableName = args[1];

                    boolean created = state.getActiveDatabase().createTable(tableName, columnTypes);
                    if (created) {
                        state.getOutputStream().println("created");
                    } else {
                        throw new DatabaseIOException(tableName + " exists");
                    }
                }
            };
    public static final Command<SingleDatabaseShellState> DROP =
            new AbstractCommand<SingleDatabaseShellState>(
                    "drop", "<tablename>", "deletes table with the given name from file system", 2) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, final String[] args)
                        throws IOException {
                    state.getActiveDatabase().dropTable(args[1]);
                    state.getOutputStream().println("dropped");
                }
            };
    public static final Command<SingleDatabaseShellState> EXIT =
            new AbstractCommand<SingleDatabaseShellState>(
                    "exit", null, "rollbacks all changes and stops interpretation", 1) {
                @Override
                public void execute(SingleDatabaseShellState state, String[] args) throws TerminalException {
                    state.prepareToExit(0);

                    // If all contracts are honoured, this line should not be reached.
                    throw new AssertionError("Exit request not thrown");
                }

                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws DatabaseIOException, IllegalArgumentException {
                    // Not used.
                }
            };
    public static final Command<SingleDatabaseShellState> GET =
            new AbstractCommand<SingleDatabaseShellState>("get", "<key>", "obtains value by the key", 2) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, final String[] args)
                        throws NoActiveTableException {
                    String key = args[1];

                    Table table = state.getActiveTable();
                    TableProvider provider = state.getProvider();

                    Storeable value = table.get(key);

                    if (value == null) {
                        throw new IllegalArgumentException("not found");
                    } else {
                        state.getOutputStream().println("found");
                        state.getOutputStream().println(provider.serialize(table, value));
                    }
                }
            };
    public static final Command<SingleDatabaseShellState> HELP =
            new AbstractCommand<SingleDatabaseShellState>(
                    "help", null, "prints out description of state commands", 1, Integer.MAX_VALUE) {
                @Override
                public void execute(SingleDatabaseShellState state, String[] args) {
                    Map<String, Command<SingleDatabaseShellState>> commands = state.getCommands();

                    state.getOutputStream().println(
                            "DatabaseLibrary is an utility that lets you work with a simple database");

                    state.getOutputStream().println(
                            String.format(
                                    "You can set database directory to work with using environment "
                                    + "variable '%s'", SingleDatabaseShellState.DB_DIRECTORY_PROPERTY_NAME));

                    commands.values()
                            .forEach((command) -> state.getOutputStream().println(command.buildHelpLine()));
                }

                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws DatabaseIOException {
                    // not used
                }
            };
    public static final Command<SingleDatabaseShellState> LIST =
            new AbstractCommand<SingleDatabaseShellState>(
                    "list", null, "prints all keys stored in the map", 1) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws NoActiveTableException {
                    List<String> keySet = state.getActiveTable().list();
                    StringBuilder sb = new StringBuilder();

                    boolean comma = false;

                    for (String key : keySet) {
                        sb.append(comma ? ", " : "").append(key);
                        comma = true;
                    }

                    state.getOutputStream().println(sb);
                }
            };
    public static final Command<SingleDatabaseShellState> PUT = new AbstractCommand<SingleDatabaseShellState>(
            "put",
            "<key> [ {boolean|number|string|null}... ]",
            "assigns new storeable to the key",
            3,
            Integer.MAX_VALUE) {
        @Override
        public void executeSafely(SingleDatabaseShellState state, String[] args)
                throws NoActiveTableException, ParseException {
            String key = args[1];

            Table table = state.getActiveTable();

            String valueStr = String.join(" ", Arrays.asList(args).subList(2, args.length));
            Storeable value = state.getProvider().deserialize(table, valueStr);

            Storeable oldValue = state.getActiveTable().put(key, value);

            if (oldValue == null) {
                state.getOutputStream().println("new");
            } else {
                String oldValueStr = state.getProvider().serialize(table, oldValue);
                state.getOutputStream().println("overwrite");
                state.getOutputStream().println("old " + oldValueStr);
            }
        }
    };
    public static final Command<SingleDatabaseShellState> REMOVE =
            new AbstractCommand<SingleDatabaseShellState>("remove", "<key>", "removes value by the key", 2) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws DatabaseIOException, NoActiveTableException {
                    String key = args[1];

                    Storeable oldValue = state.getActiveTable().remove(key);

                    if (oldValue == null) {
                        throw new DatabaseIOException("not found");
                    } else {
                        state.getOutputStream().println("removed");
                    }
                }
            };
    public static final Command<SingleDatabaseShellState> SHOW =
            new AbstractCommand<SingleDatabaseShellState>(
                    "show", "tables", "prints info on all tables assigned to the working database", 2) {
                @Override
                public void executeSafely(SingleDatabaseShellState state, String[] args)
                        throws IllegalArgumentException {
                    switch (args[1]) {
                    case "tables": {
                        state.getActiveDatabase().showTables();
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("show: unexpected option: " + args[1]);
                    }
                    }
                }
            };
    public static final Command<SingleDatabaseShellState> USE = new AbstractCommand<SingleDatabaseShellState>(
            "use",
            "<tablename>",
            "saves all changes made to the current table (if present) and makes table"
            + " with the given name the current one",
            2) {
        @Override
        public void executeSafely(final SingleDatabaseShellState state, final String[] args)
                throws IOException {
            state.getActiveDatabase().useTable(args[1]);
            state.getOutputStream().println("using " + args[1]);
        }
    };
    private static final SingleDBCommands INSTANCE = new SingleDBCommands();

    private SingleDBCommands() {

    }

    public static SingleDBCommands getInstance() {
        return INSTANCE;
    }
}
