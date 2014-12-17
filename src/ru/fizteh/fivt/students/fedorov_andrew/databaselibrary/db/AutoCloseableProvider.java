package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db;

import ru.fizteh.fivt.storage.structured.TableProvider;

public interface AutoCloseableProvider extends TableProvider, AutoCloseable {
    @Override
    void close();
}
