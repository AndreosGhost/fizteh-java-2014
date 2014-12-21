package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.Database;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExecutionNotPermittedException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.AbstractCommand;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.JoinedState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SingleDBCommands;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.SingleDatabaseShellState;

import java.util.List;

public abstract class ClientGeneralState extends JoinedState<ClientGeneralState> {
    private static final int DB_STATE_ID = 0;
    private static final int CLIENT_STATE_ID = 1;

    private final DBClientState clientState = new DBClientState();
    private SingleDatabaseShellState databaseState;

    public ClientGeneralState() {
        setAllCommands(
                SingleDBCommands.getInstance().getCommands(), ClientCommands.getInstance().getCommands());
    }

    public boolean isConnected() {
        return clientState.isConnected();
    }

    protected abstract Database obtainNewActiveDatabase() throws Exception;

    @Override
    public void init(Shell host) throws Exception {
        super.init(host);
        clientState.init(host);
        setExceptionHandler(
                (exception, noData) -> AbstractCommand.DATABASE_ERROR_HANDLER
                        .handleException(exception, getOutputStream()));
    }

    @Override
    protected void onExecuteConflict(List<Integer> stateIDs, List<Command> commands, String[] args)
            throws Exception {
        Command clientCommand = null;
        Command databaseCommand = null;

        for (int i = 0; i < stateIDs.size(); i++) {
            switch (stateIDs.get(i)) {
            case CLIENT_STATE_ID: {
                clientCommand = commands.get(i);
                break;
            }
            case DB_STATE_ID: {
                databaseCommand = commands.get(i);
                break;
            }
            default: {
                throw new IllegalArgumentException("Illegal state ID: " + stateIDs.get(i));
            }
            }
        }

        if (areNamesEqual(clientCommand, ClientCommands.HELP) && areNamesEqual(
                databaseCommand, SingleDBCommands.HELP)) {
            onExecuteRequested(CLIENT_STATE_ID, clientCommand, args);
            if (clientState.isConnected()) {
                onExecuteRequested(DB_STATE_ID, databaseCommand, args);
            }
        } else if (areNamesEqual(clientCommand, ClientCommands.EXIT) && areNamesEqual(
                databaseCommand, SingleDBCommands.EXIT)) {
            prepareToExit(0);
        } else {
            throw new UnsupportedOperationException(
                    "Cannot resolve command conflict: " + clientCommand.getName());
        }
    }

    @Override
    protected ShellState obtainState(int stateID) {
        if (stateID == CLIENT_STATE_ID) {
            return clientState;
        } else if (stateID == DB_STATE_ID) {
            return databaseState;
        } else {
            throw new IllegalArgumentException("Illegal state id: " + stateID);
        }
    }

    @Override
    protected void onExecuteRequested(int stateID, Command command, String[] args) throws Exception {
        if (stateID == CLIENT_STATE_ID) {
            executeNormally(stateID, command, args);

            if (areNamesEqual(ClientCommands.CONNECT, command)) {
                ClientGeneralState generalState = this;

                databaseState = new SingleDatabaseShellState() {

                    @Override
                    protected Database obtainNewActiveDatabase() throws Exception {
                        return generalState.obtainNewActiveDatabase();
                    }
                };
                try {
                    new Shell<>(databaseState);
                } catch (Exception exc) {
                    throw new RuntimeException(
                            "Failed to build joined state: " + exc.getMessage(), exc.getCause());
                }
            } else if (areNamesEqual(ClientCommands.DISCONNECT, command)) {
                databaseState.cleanup();
                databaseState = null;
            }
        } else if (stateID == DB_STATE_ID) {
            if (!clientState.isConnected()) {
                throw new ExecutionNotPermittedException("You should connect to a database storage at first");
            } else {
                executeNormally(stateID, command, args);
            }
        }
    }
}
