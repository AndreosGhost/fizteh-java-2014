package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.fizteh.fivt.storage.structured.RemoteTableProvider;
import ru.fizteh.fivt.storage.structured.RemoteTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.remote.RemoteDatabaseStorage;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client.ClientGeneralState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server.DBServerState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test.support.RegexMatcher;

import java.io.IOException;
import java.io.PrintStream;
import java.rmi.registry.Registry;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ClientGeneralStateTest extends InterpreterTestBase<ClientGeneralState> {
    private DBServerState serverState;
    private ClientGeneralState clientState;

    @Override
    protected Shell<ClientGeneralState> constructInterpreter() throws TerminalException {
        this.clientState = new ClientGeneralState() {
            @Override
            protected Database obtainNewActiveDatabase() throws Exception {
                RemoteTableProviderFactory factory = new RemoteDatabaseStorage();
                RemoteTableProvider provider = factory.connect("localhost", Registry.REGISTRY_PORT);
                return new Database(provider, "", getOutputStream());
            }

            @Override
            public PrintStream getOutputStream() {
                return System.out;
            }
        };
        return new Shell<>(clientState);
    }

    @Before
    public void prepareRemoteAPITest() throws Exception {
        serverState = new DBServerState(DB_ROOT.toString());
        new Shell(serverState);
        serverState.startServer(10001);
    }

    @After
    public void cleanupRemoteAPITest() throws IOException {
        serverState.stopServer();
    }

    @Test
    public void testCreateTableAndPutSmth() throws IOException, TerminalException {
        runBatchExpectZero(
                "connect localhost 10001",
                "create t1 (String)",
                "use t1",
                "put a [\"b\"]",
                "commit");
        assertEquals(
                makeTerminalExpectedMessage("connected", "created", "using t1", "new", "1"),
                getOutput());
        runBatchExpectZero("connect localhost 10001", "use t1", "get a");
        assertEquals(makeTerminalExpectedMessage("connected", "using t1", "found", "[\"b\"]"), getOutput());
    }

    @Test
    public void testConnectToNotExistentServer() throws IOException, TerminalException {
        runBatchExpectNonZero("connect 127.0.0.1 10500");
        assertThat(getOutput(), startsWith("not connected"));
    }

    @Test
    public void testConnectAndCallNotExistentCommand() throws IOException, TerminalException {
        runInteractiveExpectZero("connect 127.0.0.1 10001", "not_exists_yeah?", "disconnect");
        assertThat(
                getOutput(), new RegexMatcher(
                        makeTerminalExpectedRegex(
                                "\\Q" + clientState.getGreetingString() + "\\E",
                                "connected",
                                "\\Qnot_exists_yeah?: command is missing\\E",
                                "disconnected")));
    }

    @Test
    public void testCallShowTablesWithoutConnect() throws IOException, TerminalException {
        runBatchExpectNonZero("show tables");
        assertEquals(
                getOutput(), makeTerminalExpectedMessage(
                        "You should connect to a database storage at first"));
    }
}
