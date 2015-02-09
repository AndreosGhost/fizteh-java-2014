package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.AutoCloseableTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.DBTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server.TelnetDBServerState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DBServerTest extends TestBase {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private TelnetDBServerState serverState;

    private AutoCloseableTableProviderFactory factory;

    @Before
    public void prepareRemoteAPITest() throws Exception {
        factory = new DBTableProviderFactory();
        TableProvider provider = factory.create(DB_ROOT.toString());

        serverState = new TelnetDBServerState(provider, DB_ROOT.toString());
        new Shell(serverState);
    }

    @After
    public void cleanupRemoteAPITest() throws IOException {
        serverState.stopServerIfStarted();
        factory.close();
    }

    private String collectFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while (reader.ready()) {
                sb.append(reader.readLine()).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    @Test
    public void testDoubleStart() throws IOException {
        serverState.startServer(10001);
        exception.expect(IllegalStateException.class);
        exception.expectMessage("not started: already started");
        serverState.startServer(10002);
    }

    @Test
    public void testDisconnectNotConnected() throws IOException {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("not started");
        serverState.stopServer();
    }

    @Test
    public void testStartStop() throws IOException {
        serverState.startServer(10001);
        assertTrue(serverState.isStarted());
        serverState.stopServer();
        assertFalse(serverState.isStarted());
    }

    @Test
    public void testStopIfConnected() throws IOException {
        serverState.stopServerIfStarted();
    }

    @Test
    public void testStopIfConnected2() throws IOException {
        serverState.startServer(10001);
        assertTrue(serverState.isStarted());
        serverState.stopServerIfStarted();
        assertFalse(serverState.isStarted());
    }
}
