package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote.RemoteDatabaseStorage;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote.RemoteTableProviderFactoryImpl;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvalidatedObjectException;

import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RemoteDataStorageTest extends TestBase {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RemoteTableProviderFactoryImpl factory;

    @Before
    public void prepare() throws Exception {
        factory = new RemoteTableProviderFactoryImpl();
        factory.establishStorage(DB_ROOT.toString());
    }

    @After
    public void cleanup() throws Exception {
        factory.close();
        cleanDBRoot();
    }

    @Test
    public void testProviderStubsAreSame() throws IOException {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider providerA = storage.connect("localhost", Registry.REGISTRY_PORT);
        RemoteTableProvider providerB = storage.connect("127.0.0.1", Registry.REGISTRY_PORT);
        assertSame(providerA, providerB);
    }

    @Test
    public void testProviderStubsClosedProperly() throws IOException {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider providerA = storage.connect("localhost", Registry.REGISTRY_PORT);
        providerA.close();
        RemoteTableProvider providerB = storage.connect("localhost", Registry.REGISTRY_PORT);

        assertNotSame(providerA, providerB);

        // Check it is actual.
        providerB.getTableNames();

        exception.expect(InvalidatedObjectException.class);

        // It was closed = invalidated.
        providerA.getTableNames();
    }

    @Test
    public void testTableStubsAreSame() throws Exception {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider provider = storage.connect("localhost", Registry.REGISTRY_PORT);

        String tableName = "table";

        Table remoteTableA = provider.createTable(tableName, Arrays.asList(String.class));
        Table remoteTableB = provider.getTable(tableName);
        Table remoteTableC = provider.getTable(tableName);

        assertSame(remoteTableA, remoteTableB);
        assertSame(remoteTableB, remoteTableC);
    }

    @Test
    public void testServerTableCloseInvalidatesRemoteTable() throws Exception {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider provider = storage.connect("localhost", Registry.REGISTRY_PORT);

        String tableName = "table";

        Table remoteTable = provider.createTable(tableName, Arrays.asList(String.class));

        Table serverTable = factory.getProvider().getTable(tableName);
        ((AutoCloseable) serverTable).close();

        exception.expect(InvalidatedObjectException.class);
        remoteTable.getName();
    }

    @Test
    public void testServerFactoryCloseInvalidatesRemoteTable() throws Exception {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider provider = storage.connect("localhost", Registry.REGISTRY_PORT);

        String tableName = "table";

        Table remoteTable = provider.createTable(tableName, Arrays.asList(String.class));

        factory.close();

        exception.expect(InvalidatedObjectException.class);
        remoteTable.getName();
    }

    @Test
    public void testRemoteTableClosesAfterTableIsInvalidated() throws Exception {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider remoteProvider = storage.connect("localhost", Registry.REGISTRY_PORT);

        String tableName = "table";

        Table remoteTable = remoteProvider.createTable(tableName, Arrays.asList(String.class));

        Table serverTable = factory.getProvider().getTable(tableName);
        ((AutoCloseable) serverTable).close();

        try {
            remoteTable.list();
        } catch (InvalidatedObjectException exc) {
            // Ignore it.
        }

        // Now client's remoteTable must have been completely closed.
        Table remoteTable2 = remoteProvider.getTable(tableName);

        // Checking availability.
        remoteTable2.getName();

        exception.expect(InvalidatedObjectException.class);
        remoteTable.getName();
    }

    @Test
    public void testTwoCreatesOfTheSameTable() throws IOException {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider providerA = storage.connect("localhost", Registry.REGISTRY_PORT);
        RemoteTableProvider providerB = storage.connect("localhost", Registry.REGISTRY_PORT);

        String tableName = "table";

        Table remoteTableA = providerA.createTable(tableName, Arrays.asList(String.class));
        Table remoteTableB = providerB.createTable(tableName, Arrays.asList(String.class));

        assertNull(remoteTableB);
    }

    @Test
    public void testPutValue() throws IOException {
        RemoteDatabaseStorage storage = new RemoteDatabaseStorage();
        RemoteTableProvider providerA = storage.connect("localhost", Registry.REGISTRY_PORT);
        RemoteTableProvider providerB = storage.connect("localhost", Registry.REGISTRY_PORT);

        String tableName = "table";
        String key = "key";
        String value = "value";

        providerA.createTable(tableName, Arrays.asList(String.class));

        Table tableFromA = providerA.getTable(tableName);
        Table tableFromB = providerB.getTable(tableName);

        tableFromA.put(key, providerA.createFor(tableFromA, Arrays.asList(value)));
        Storeable storeable = tableFromB.get(key);

        assertEquals(value, storeable.getStringAt(0));
    }
}
