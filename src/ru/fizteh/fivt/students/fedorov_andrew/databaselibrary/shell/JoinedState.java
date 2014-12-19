package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExitRequest;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.AccurateExceptionHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class JoinedState<S extends ShellState<S>> extends BaseShellState<S> {
    private Map<String, Command<S>> allCommands;
    private int statesCount;

    private AccurateExceptionHandler<Void> exceptionHandler;

    public JoinedState(AccurateExceptionHandler<Void> exceptionHandler,
                       Map<String, ? extends Command<? extends ShellState>>... commandsMaps) {
        this(exceptionHandler);
        setAllCommands(commandsMaps);
    }

    public JoinedState(AccurateExceptionHandler<Void> exceptionHandler) {
        setExceptionHandler(exceptionHandler);
    }

    public JoinedState(Map<String, ? extends Command<? extends ShellState>>... commandsMaps) {
        setAllCommands(commandsMaps);
    }

    public JoinedState() {

    }

    public AccurateExceptionHandler<Void> getExceptionHandler() {
        return exceptionHandler;
    }

    protected void setExceptionHandler(AccurateExceptionHandler<Void> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    protected final void setAllCommands(Map<String, ? extends Command<? extends ShellState>>...
                                                commandsMaps) {
        this.statesCount = commandsMaps.length;
        Map<String, Command<S>> allCommandsMap = new HashMap<>();

        int id = 0;
        for (Map<String, ? extends Command<? extends ShellState>> commands : commandsMaps) {
            int stateID = id;
            commands.forEach(
                    (name, command) -> {
                        if (allCommandsMap.containsKey(name)) {
                            ((CommandWrapper) allCommandsMap.get(name)).addCommandAndState(stateID, command);
                        } else {
                            allCommandsMap.put(name, new CommandWrapper(stateID, command));
                        }
                    });
            id++;
        }

        allCommands = Collections.unmodifiableMap(allCommandsMap);
    }

    /**
     * Calls {@link ShellState#cleanup()} on each registered state if it is not null.<br/>
     * States are obtained by {@link #obtainState(int)}.
     */
    @Override
    public void cleanup() {
        for (int stateID = 0; stateID < statesCount; stateID++) {
            ShellState state = obtainState(stateID);
            if (state != null) {
                state.cleanup();
            }
        }
    }

    /**
     * Safely calls {@link ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell
     * .ShellState#prepareToExit(int)}
     * on each registered state if it is not null.<br/>
     * States are obtained by {@link #obtainState(int)}.<br/>
     * The resulting exit code depends on states' implementations.
     */
    @Override
    public void prepareToExit(int exitCode) throws ExitRequest {
        int code = exitCode;
        for (int stateID = 0; stateID < statesCount; stateID++) {
            ShellState state = obtainState(stateID);
            if (state != null) {
                try {
                    state.prepareToExit(exitCode);
                    throw new AssertionError(
                            "prepareToExit() contract not honoured by " + state.getClass());
                } catch (ExitRequest exitRequest) {
                    code = exitRequest.getCode();
                }
            }
        }

        throw new ExitRequest(code);
    }

    @Override
    public Map<String, Command<S>> getCommands() {
        return allCommands;
    }

    protected boolean areNamesEqual(Command commandA, Command commandB) {
        return Objects.equals(commandA.getName(), commandB.getName());
    }

    /**
     * This method is called when there are several commands with the same name from different states. You
     * decide how to resolve this conflict.
     * @throws Exception
     */
    protected void onExecuteConflict(List<Integer> stateIDs, List<Command> commands, String[] args)
            throws Exception {
        throw new UnsupportedOperationException("Execute conflicts are not resolved");
    }

    protected void executeNormally(int stateID, Command command, String[] args) throws Exception {
        command.execute(obtainState(stateID), args);
    }

    protected abstract ShellState obtainState(int stateID);

    /**
     * This method is called when one of commands is requested to invoke. You decide whether it happens.<br/>
     * Default implementation of this method executes any given command.
     * @param stateID
     *         id of host state for the command.
     * @param command
     *         command that is requested to invoke
     * @param args
     *         arguments given to the command.
     */
    protected void onExecuteRequested(int stateID, Command command, String[] args) throws Exception {
        executeNormally(stateID, command, args);
    }

    class CommandWrapper implements Command {
        private List<Integer> states = new LinkedList<>();
        private List<Command> commands = new LinkedList<>();

        public CommandWrapper(int stateID, Command wrappedCommand) {
            states.add(stateID);
            commands.add(wrappedCommand);
        }

        public void addCommandAndState(int state, Command command) {
            states.add(state);
            commands.add(command);
        }

        @Override
        public void execute(ShellState state, String[] args) throws TerminalException {
            try {
                if (states.size() == 1) {
                    onExecuteRequested(states.get(0), commands.get(0), args);
                } else {
                    onExecuteConflict(
                            Collections.unmodifiableList(states),
                            Collections.unmodifiableList(commands),
                            args);
                }
            } catch (TerminalException handledException) {
                throw handledException;
            } catch (Exception exc) {
                exceptionHandler.handleException(exc, null);
            }
        }

        @Override
        public String getName() {
            return commands.get(0).getName();
        }

        @Override
        public String getInfo() {
            throw new UnsupportedOperationException("You cannot operate with multi command directly.");
        }

        @Override
        public String getInvocation() {
            throw new UnsupportedOperationException("You cannot operate with multi command directly.");
        }
    }
}
