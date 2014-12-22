package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExecutionNotPermittedException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.HttpDBServerCommands;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.HttpDBServerState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.AbstractCommand;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.JoinedState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Shell;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client.ClientCommands;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.client.ClientGeneralState;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server.ServerCommands;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.telnet.server.TelnetDBServerState;

import java.util.List;

public class DBShellGeneralState extends JoinedState<DBShellGeneralState> {
    private static final int CLIENT_STATE_ID = 0;
    private static final int TELNET_SERVER_STATE_ID = 1;
    private static final int HTTP_SERVER_STATE_ID = 2;

    private ClientGeneralState clientState;
    private TelnetDBServerState telnetServerState;
    private HttpDBServerState httpServerState;

    public DBShellGeneralState(TableProvider provider, String databaseRoot) throws TerminalException {
        //TODO: we need remote provider here.

        clientState = new ClientGeneralState();
        telnetServerState = new TelnetDBServerState(provider, databaseRoot);
        httpServerState = new HttpDBServerState(provider);

        // Fake initialization.
        new Shell<>(clientState);
        new Shell<>(telnetServerState);
        new Shell<>(httpServerState);

        setAllCommands(
                clientState.getCommands(), telnetServerState.getCommands(), httpServerState.getCommands());
        setExceptionHandler(
                (exception, noData) -> AbstractCommand.DEFAULT_EXCEPTION_HANDLER
                        .handleException(exception, getOutputStream()));
    }

    @Override
    protected void onExecuteConflict(List<Integer> stateIDs, List<Command> commands, String[] args)
            throws Exception {
        Command telnetServerCommand = null;
        Command clientCommand = null;
        Command httpServerCommand = null;

        for (int i = 0; i < stateIDs.size(); i++) {
            switch (stateIDs.get(i)) {
            case CLIENT_STATE_ID: {
                clientCommand = commands.get(i);
                break;

            }
            case TELNET_SERVER_STATE_ID: {
                telnetServerCommand = commands.get(i);
                break;
            }
            case HTTP_SERVER_STATE_ID: {
                httpServerCommand = commands.get(i);
                break;
            }
            default: {
                throw new IllegalArgumentException("Illegal state ID: " + stateIDs.get(i));
            }
            }
        }

        boolean telnetServerActive = telnetServerState.isStarted();
        boolean clientActive = clientState.isConnected();
        boolean httpServerActive = httpServerState.isStarted();

        boolean nobodyActive = !telnetServerActive && !clientActive && !httpServerActive;

        boolean helpCommand = areNamesEqual(telnetServerCommand, ServerCommands.HELP) && areNamesEqual(
                clientCommand, ClientCommands.HELP) && areNamesEqual(
                httpServerCommand, HttpDBServerCommands.HELP);
        boolean exitCommand = areNamesEqual(telnetServerCommand, ServerCommands.EXIT) && areNamesEqual(
                clientCommand, ClientCommands.EXIT) && areNamesEqual(
                httpServerCommand, HttpDBServerCommands.EXIT);

        // Conflict between server and client EXIT or HELP command.
        //        if (telnetServerActive || httpServerActive) {
        //            if (telnetServerActive) {
        //                // Telnet Server variant
        //                onExecuteRequested(TELNET_SERVER_STATE_ID, telnetServerCommand, args);
        //            }
        //            if (httpServerActive) {
        //                onExecuteRequested(HTTP_SERVER_STATE_ID, httpServerCommand, args);
        //            }
        //        } else if (clientActive) {
        //            // Client variant
        //            onExecuteRequested(CLIENT_STATE_ID, clientCommand, args);
        //        } else
        if (helpCommand) {
            if (clientActive || nobodyActive) {
                executeNormally(CLIENT_STATE_ID, clientCommand, args);
            }
            if (!clientActive || nobodyActive) {
                executeNormally(HTTP_SERVER_STATE_ID, httpServerCommand, args);
                executeNormally(TELNET_SERVER_STATE_ID, telnetServerCommand, args);
            }
        } else if (exitCommand) {
            prepareToExit(0);
        } else {
            throw new UnsupportedOperationException(
                    "Cannot resolve command conflict: "
                    + telnetServerCommand.getName()
                    + " and "
                    + clientCommand.getName());
        }
    }

    @Override
    protected ShellState obtainState(int stateID) {
        switch (stateID) {
        case CLIENT_STATE_ID:
            return clientState;
        case TELNET_SERVER_STATE_ID:
            return telnetServerState;
        case HTTP_SERVER_STATE_ID:
            return httpServerState;
        default:
            throw new IllegalArgumentException("Illegal state ID: " + stateID);
        }
    }

    @Override
    protected void onExecuteRequested(int stateID, Command command, String[] args) throws Exception {
        if (stateID == CLIENT_STATE_ID) {
            if (telnetServerState.isStarted() || httpServerState.isStarted()) {
                throw new ExecutionNotPermittedException("You cannot execute this command in server mode");
            } else {
                executeNormally(stateID, command, args);
            }
        } else {
            if (clientState.isConnected()) {
                throw new ExecutionNotPermittedException(
                        "You cannot execute this command when connected as client");
            } else {
                executeNormally(stateID, command, args);
            }
        }
    }
}
