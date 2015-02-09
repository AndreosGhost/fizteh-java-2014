package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db;

import ru.fizteh.fivt.storage.structured.TableProvider;

import java.io.IOException;
import java.util.List;

public interface AutoCloseableProvider extends TableProvider, AutoCloseable {
    @Override
    void close();

    @Override
    AutoCloseableTable getTable(String name);

    @Override
    AutoCloseableTable createTable(String name, List<Class<?>> columnTypes) throws IOException;
}
