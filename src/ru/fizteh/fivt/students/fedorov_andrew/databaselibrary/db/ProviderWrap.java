package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvalidatedObjectException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.KillLock;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.ValidityController.UseLock;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Wraps the given table provider.<br/>
 * {@link ProviderWrap#close()} method just invalidates the wrap. Original provider is not affected.<br/>
 * Also returned tables are wraps that can be closed without affecting original tables.
 */
public class ProviderWrap implements AutoCloseableProvider {
    private final TableProvider provider;

    private final ValidityController providerVC = new ValidityController();

    public ProviderWrap(TableProvider provider) {
        this.provider = provider;
    }

    @Override
    public void removeTable(String name) throws IOException {
        try (UseLock lock = providerVC.use()) {
            provider.removeTable(name);
        }
    }

    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        try (UseLock lock = providerVC.use()) {
            return provider.deserialize(table, value);
        }
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        try (UseLock lock = providerVC.use()) {
            return provider.serialize(table, value);
        }
    }

    @Override
    public Storeable createFor(Table table) {
        try (UseLock lock = providerVC.use()) {
            return provider.createFor(table);
        }
    }

    @Override
    public Storeable createFor(Table table, List<?> values)
            throws ColumnFormatException, IndexOutOfBoundsException {
        try (UseLock lock = providerVC.use()) {
            return provider.createFor(table, values);
        }
    }

    @Override
    public List<String> getTableNames() {
        try (UseLock lock = providerVC.use()) {
            return provider.getTableNames();
        }
    }

    @Override
    public void close() {
        try (KillLock lock = providerVC.useAndKill()) {
            // Killing.
        }
    }

    @Override
    public AutoCloseableTable getTable(String name) {
        try (UseLock lock = providerVC.use()) {
            Table table = provider.getTable(name);
            return table == null ? null : new TableWrap(table);
        }
    }

    @Override
    public AutoCloseableTable createTable(String name, List<Class<?>> columnTypes) throws IOException {
        try (UseLock lock = providerVC.use()) {
            Table table = provider.createTable(name, columnTypes);
            return table == null ? null : new TableWrap(table);
        }
    }

    class TableWrap implements AutoCloseableTable {
        private final Table table;
        private final ValidityController tableVC = new ValidityController();

        public TableWrap(Table table) {
            this.table = table;
        }

        private void forceClose(UseLock tableLock) {
            try (KillLock lock = tableLock.obtainKillLockInstead()) {
                close();
            }
        }

        @Override
        public void close() {
            try (KillLock tableLock = tableVC.useAndKill()) {
                // Killing.
            }
        }

        @Override
        public Storeable put(String key, Storeable value) throws ColumnFormatException {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.put(key, value);
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public Storeable remove(String key) {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.remove(key);
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public int size() {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.size();
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public List<String> list() {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.list();
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public int commit() throws IOException {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.commit();
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public int rollback() {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.rollback();
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public int getNumberOfUncommittedChanges() {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.getNumberOfUncommittedChanges();
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public int getColumnsCount() {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.getColumnsCount();
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.getColumnType(columnIndex);
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public String getName() {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.getName();
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }

        @Override
        public Storeable get(String key) {
            try (UseLock tableLock = tableVC.use()) {
                try (UseLock providerLock = providerVC.use()) {
                    return table.get(key);
                } catch (InvalidatedObjectException exc) {
                    forceClose(tableLock);
                    throw exc;
                }
            }
        }
    }
}
