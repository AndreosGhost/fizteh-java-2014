package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExitRequest;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.NoActiveTableException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.PrintStream;
import java.util.Map;

/**
 * This class represents actual task implementation: work from terminal with a database, whose
 * location in file system is given.
 */
public abstract class SingleDatabaseShellState extends BaseShellState<SingleDatabaseShellState> {
    /**
     * Name of environment property; value stored there is database location.
     */
    public static final String DB_DIRECTORY_PROPERTY_NAME = "fizteh.db.dir";
    /**
     * Our proxy command container.
     */
    private static final SingleDBCommands COMMANDS_CONTAINER = SingleDBCommands.getInstance();

    /**
     * Database that user works with via terminal.
     */
    private Database activeDatabase;

    @Override
    public PrintStream getOutputStream() {
        checkInitialized();
        return host.getOutputStream();
    }

    @Override
    public String getGreetingString() {
        Table table;
        try {
            table = getActiveDatabase().getActiveTable();
        } catch (NoActiveTableException exc) {
            table = null;
        }
        return String.format(
                "%s%s $ ",
                (table == null ? "" : (table.getName() + '@')),
                getActiveDatabase().getDbDirectory());
    }

    @Override
    public void init(Shell<SingleDatabaseShellState> host) throws Exception {
        super.init(host);

        activeDatabase = obtainNewActiveDatabase();
    }

    protected abstract Database obtainNewActiveDatabase() throws Exception;

    @Override
    public void cleanup() {
        getActiveDatabase().rollback();
    }

    @Override
    public void prepareToExit(int exitCode) throws ExitRequest {
        Log.log(SingleDatabaseShellState.class, "Preparing to exit with code " + exitCode);
        cleanup();
        Log.close();
        throw new ExitRequest(exitCode);
    }

    /**
     * Returns database user works with.
     */
    public Database getActiveDatabase() {
        checkInitialized();
        return activeDatabase;
    }

    public TableProvider getProvider() {
        return getActiveDatabase().getProvider();
    }

    public Table getActiveTable() throws NoActiveTableException {
        return getActiveDatabase().getActiveTable();
    }

    @Override
    public Map<String, Command<SingleDatabaseShellState>> getCommands() {
        return COMMANDS_CONTAINER.getCommands();
    }
}
