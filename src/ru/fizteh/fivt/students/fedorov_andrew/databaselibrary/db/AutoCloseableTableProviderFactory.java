package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.Closeable;
import java.io.IOException;

public interface AutoCloseableTableProviderFactory extends TableProviderFactory, Closeable {
    @Override
    AutoCloseableProvider create(String path) throws IOException;
}
