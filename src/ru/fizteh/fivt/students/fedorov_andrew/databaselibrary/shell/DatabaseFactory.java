package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.DBTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;

import java.io.Closeable;
import java.io.PrintStream;

public final class DatabaseFactory implements Closeable {
    private final DBTableProviderFactory factory;
    private final TableProvider provider;
    private final String databasePath;

    public DatabaseFactory() throws Exception {
        factory = new DBTableProviderFactory();
        databasePath = System.getProperty(SingleDatabaseShellState.DB_DIRECTORY_PROPERTY_NAME);
        provider = factory.create(databasePath);
    }

    public Database obtainDatabase(PrintStream outputStream) {
        return new Database(provider, databasePath, outputStream);
    }

    @Override
    public void close() {
        factory.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
