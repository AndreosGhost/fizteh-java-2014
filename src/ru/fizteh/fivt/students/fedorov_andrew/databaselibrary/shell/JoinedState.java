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

/**
 * State that encapsulates several states that can be used. You need to create shell interpreter only for the
 * joined state. You can decide when and how to react to command invocation requests to any state.<br/>
 * If a command during execution throws {@link ru.fizteh.fivt.students.fedorov_andrew.databaselibrary
 * .exception.ExitRequest} it is not handled by this default implementation.
 * @param <S>
 *         extending state for which interpreter is going to be created.
 */
public abstract class JoinedState<S extends ShellState<S>> extends BaseShellState<S> {
    /**
     * Wrapped commands
     */
    private Map<String, Command<S>> allCommands;
    private int statesCount;

    private AccurateExceptionHandler<Void> exceptionHandler;

    /**
     * Pure state with no commands and exception handler.
     */
    protected JoinedState() {
    }

    /**
     * Obtains currently used exception handler.
     */
    public AccurateExceptionHandler<Void> getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Sets the exception handler that is used to handle errors during command executions.
     * @param exceptionHandler
     *         exception handler with no extra data.
     */
    protected void setExceptionHandler(AccurateExceptionHandler<Void> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Sets all acceptable commands that can be invoked.
     * @param commandsMaps
     *         array of command maps. Each command map is associated with state id (that is equal to the
     *         order
     *         number in this parameters sequence) which is used further to distinguish commands.
     */
    protected final void setAllCommands(Map<String, ? extends Command<? extends ShellState>>...
                                                commandsMaps) {
        Map<String, Command<S>> allCommandsMap = new HashMap<>();

        int idCounter = 0;
        for (Map<String, ? extends Command<? extends ShellState>> commands : commandsMaps) {
            int stateID = idCounter;
            commands.forEach(
                    (name, command) -> {
                        if (allCommandsMap.containsKey(name)) {
                            ((CommandWrapper) allCommandsMap.get(name)).addCommandAndState(stateID, command);
                        } else {
                            allCommandsMap.put(name, new CommandWrapper(stateID, command));
                        }
                    });
            idCounter++;
        }

        allCommands = Collections.unmodifiableMap(allCommandsMap);
        statesCount = commandsMaps.length;
    }

    /**
     * Calls {@link ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState#cleanup()} on
     * each
     * registered state if it is not null.<br/>
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

    /**
     * Returns mapping between command names and command wrappers that on {@link
     * ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command#execute(ru.fizteh.fivt.students
     * .fedorov_andrew.databaselibrary.shell.ShellState,
     * String[])} calls one of two methods: {@link #onExecuteConflict(java.util.List, java.util.List,
     * String[])} or {@link #onExecuteRequested(int, ru.fizteh.fivt.students.fedorov_andrew.databaselibrary
     * .shell.Command,
     * String[])}.<br/>
     * Exceptions thrown by these methods are handled with local exception handler.<br/>
     * As soon as all possible state commands wrapped by one multicommand have the same name, {@link
     * ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command#getName()} returns that name, but
     * both {@link ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command#getInfo()} and {@link
     * ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command#getInvocation()} throw {@link
     * UnsupportedOperationException}.
     * @see #setExceptionHandler(AccurateExceptionHandler)
     * @see #getExceptionHandler()
     * @see #onExecuteRequested(int, ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.Command,
     * String[])
     * @see #onExecuteConflict(java.util.List, java.util.List, String[])
     */
    @Override
    public Map<String, Command<S>> getCommands() {
        return allCommands;
    }

    /**
     * Convenience method.
     * Equivalent to {@code Objects.equals(commandA.getName(), commandB.getName())}.
     */
    protected boolean areNamesEqual(Command commandA, Command commandB) {
        return Objects.equals(commandA.getName(), commandB.getName());
    }

    /**
     * This method is called when there are several commands with the same name from different states. You
     * decide how to resolve this conflict. <br/>
     * The default implementation throws {@link UnsupportedOperationException}.<br/>
     * In this method you can also call {@link #onExecuteRequested(int, ru.fizteh.fivt.students
     * .fedorov_andrew.databaselibrary.shell.Command,
     * String[])} after you have
     * chosen for which state to call the command.
     * @throws Exception
     */
    protected void onExecuteConflict(List<Integer> stateIDs, List<Command> commands, String[] args)
            throws Exception {
        throw new UnsupportedOperationException("Execute conflicts are not resolved");
    }

    /**
     * Runs normal execution of the given state command. The default implementation is {@code
     * command.execute(obtainState(stateID), args)}.
     * @throws Exception
     */
    protected void executeNormally(int stateID, Command command, String[] args) throws Exception {
        command.execute(obtainState(stateID), args);
    }

    /**
     * Obtains existing or new state for the given state id.
     */
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

    /**
     * Command for {@link JoinedState} that
     * wraps
     * at least one command from a state.
     */
    class CommandWrapper implements Command<S> {
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
            throw new UnsupportedOperationException("You cannot operate with command wrapper directly.");
        }

        @Override
        public String getInvocation() {
            throw new UnsupportedOperationException("You cannot operate with command wrapper directly.");
        }
    }
}
