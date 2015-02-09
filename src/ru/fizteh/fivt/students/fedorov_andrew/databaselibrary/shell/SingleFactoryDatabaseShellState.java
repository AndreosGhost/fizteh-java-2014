package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.DBTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;

public class SingleFactoryDatabaseShellState extends SingleDatabaseShellState {

    private final DBTableProviderFactory factory;

    public SingleFactoryDatabaseShellState() {
        this.factory = new DBTableProviderFactory();
    }

    @Override
    protected Database obtainNewActiveDatabase() throws Exception {
        String dbPath = System.getProperty(DB_DIRECTORY_PROPERTY_NAME);
        if (dbPath == null) {
            throw new IllegalStateException("Please mention database directory");
        }
        TableProvider provider = factory.create(dbPath);
        return new Database(provider, dbPath, getOutputStream());
    }

    @Override
    public void cleanup() {
        super.cleanup();
        factory.close();
    }
}
