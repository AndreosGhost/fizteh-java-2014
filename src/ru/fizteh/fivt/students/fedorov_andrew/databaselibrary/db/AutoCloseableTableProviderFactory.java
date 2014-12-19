package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;

import java.io.Closeable;

public interface AutoCloseableTableProviderFactory extends TableProviderFactory, Closeable {}
