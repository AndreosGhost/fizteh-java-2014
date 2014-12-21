package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExecutionNotPermittedException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.AbstractCommand;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.JoinedState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client.ClientCommands;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client.ClientGeneralState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server.DBServerState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server.ServerCommands;

import java.util.List;

public abstract class ClientServerGeneralState extends JoinedState<ClientServerGeneralState> {
    private static final int CLIENT_STATE_ID = 0;
    private static final int SERVER_STATE_ID = 1;

    private ClientGeneralState clientState;
    private DBServerState serverState;

    public ClientServerGeneralState(String databaseRoot) throws TerminalException {
        ClientServerGeneralState genState = this;
        clientState = new ClientGeneralState() {
            @Override
            protected Database obtainNewActiveDatabase() throws Exception {
                return genState.obtainNewActiveDatabase();
            }
        };
        serverState = new DBServerState(databaseRoot);

        new Shell<>(clientState);
        new Shell<>(serverState);

        setAllCommands(clientState.getCommands(), serverState.getCommands());
        setExceptionHandler(
                (exception, noData) -> AbstractCommand.DATABASE_ERROR_HANDLER
                        .handleException(exception, getOutputStream()));
    }

    @Override
    protected void onExecuteConflict(List<Integer> stateIDs, List<Command> commands, String[] args)
            throws Exception {
        Command serverCommand = null;
        Command clientCommand = null;

        for (int i = 0; i < stateIDs.size(); i++) {
            switch (stateIDs.get(i)) {
            case CLIENT_STATE_ID: {
                clientCommand = commands.get(i);
                break;

            }
            case SERVER_STATE_ID: {
                serverCommand = commands.get(i);
                break;
            }
            default: {
                throw new IllegalArgumentException("Illegal state ID: " + stateIDs.get(i));
            }
            }
        }

        boolean serverActive = serverState.isStarted();
        boolean clientActive = clientState.isConnected();

        // Conflict between server and client EXIT or HELP command.
        if (serverActive && !clientActive) {
            // Server variant
            onExecuteRequested(SERVER_STATE_ID, serverCommand, args);
        } else if (clientActive && !serverActive) {
            // Client variant
            onExecuteRequested(CLIENT_STATE_ID, clientCommand, args);
        } else if (areNamesEqual(serverCommand, ServerCommands.HELP) && areNamesEqual(
                clientCommand, ClientCommands.HELP)) {
            executeNormally(SERVER_STATE_ID, serverCommand, args);
            executeNormally(CLIENT_STATE_ID, clientCommand, args);
        } else if (areNamesEqual(serverCommand, ServerCommands.EXIT) && areNamesEqual(
                clientCommand, ClientCommands.EXIT)) {
            prepareToExit(0);
        } else {
            throw new UnsupportedOperationException(
                    "Cannot resolve command conflict: " + serverCommand.getName() + " and " + clientCommand
                            .getName());
        }
    }

    @Override
    protected ShellState obtainState(int stateID) {
        switch (stateID) {
        case CLIENT_STATE_ID:
            return clientState;
        case SERVER_STATE_ID:
            return serverState;
        default:
            throw new IllegalArgumentException("Illegal state ID: " + stateID);
        }
    }

    @Override
    protected void onExecuteRequested(int stateID, Command command, String[] args) throws Exception {
        if (stateID == CLIENT_STATE_ID) {
            if (serverState.isStarted()) {
                throw new ExecutionNotPermittedException("You cannot execute this command in server mode");
            } else {
                executeNormally(stateID, command, args);
            }
        } else if (stateID == SERVER_STATE_ID) {
            if (clientState.isConnected()) {
                throw new ExecutionNotPermittedException(
                        "You cannot execute this command when connected as client");
            } else {
                executeNormally(stateID, command, args);
            }
        }
    }

    protected abstract Database obtainNewActiveDatabase() throws Exception;
}
