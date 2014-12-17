package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test.support;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.DBTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.DatabaseIOException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SingleDatabaseShellState;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Mutated SingleDatabaseShellState that limits calls to {@link ru.fizteh.fivt.students.fedorov_andrew
 * .databaselibrary.shell.ShellState#persist()}
 * method.
 */
public class MutatedSDSS extends SingleDatabaseShellState {
    private final DBTableProviderFactory factory;
    private final TableProvider provider;
    private final String databasePath;
    private int commitCallsLeft;
    private MutatedDatabase mutatedActiveDatabase;

    public MutatedSDSS(int commitCallsLeft) throws DatabaseIOException {
        if (commitCallsLeft < 0) {
            throw new IllegalArgumentException("commitCallsLeft must be positive or 0");
        }
        this.commitCallsLeft = commitCallsLeft;
        factory = new DBTableProviderFactory();
        databasePath = System.getProperty(DB_DIRECTORY_PROPERTY_NAME);
        provider = factory.create(databasePath);
    }

    @Override
    public PrintStream getOutputStream() {
        return System.out;
    }

    @Override
    public void init(Shell<SingleDatabaseShellState> host) throws IllegalArgumentException, IOException {
        this.mutatedActiveDatabase = new MutatedDatabase(provider, databasePath, commitCallsLeft);
    }

    @Override
    protected Database obtainNewActiveDatabase() throws Exception {
        return null;
    }

    @Override
    public void cleanup() {
        factory.close();
    }

    @Override
    public Database getActiveDatabase() {
        return mutatedActiveDatabase;
    }
}
