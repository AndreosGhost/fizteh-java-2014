package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExecutionNotPermittedException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.InvocationException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.NoActiveTableException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.UnexpectedRemoteException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.WrongArgsNumberException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.AccurateExceptionHandler;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Objects;

import static ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Utility.*;

/**
 * Convenience class for Commands.
 * @author phoenix
 */
public abstract class AbstractCommand<State extends ShellState<State>> implements Command<State> {
    private static final Class<?>[] EXECUTE_SAFELY_THROWN_EXCEPTIONS;

    static {
        Class<?>[] exceptions = null;

        try {
            exceptions = AbstractCommand.class.getDeclaredMethod(
                    "executeSafely", ShellState.class, String[].class).getExceptionTypes();
        } catch (Exception exc) {
            Log.log(AbstractCommand.class, exc, "Failed to obtain exceptions thrown by executeSafely");
            throw new RuntimeException(
                    "Failed to obtain exceptions thrown by executeSafely: " + exc.getMessage(),
                    exc.getCause());
        } finally {
            EXECUTE_SAFELY_THROWN_EXCEPTIONS = exceptions;
        }
    }

    /**
     * Used for unsafe calls. Catches and handles all exceptions thrown by {@link
     * AbstractCommand#executeSafely
     * (SingleDatabaseShellState, String[]) } and {@link IllegalArgumentException }.
     */
    public static final AccurateExceptionHandler<PrintStream> DATABASE_ERROR_HANDLER =
            (Exception exc, PrintStream ps) -> {
                boolean found = false;
                Class<?> actualType = exc.getClass();

                for (Class<?> expectedType : EXECUTE_SAFELY_THROWN_EXCEPTIONS) {
                    try {
                        actualType.asSubclass(expectedType);
                        found = true;
                        break;
                    } catch (ClassCastException cce) {
                        // Ignore it.
                    }
                }

                if (found) {
                    Shell.handleError(exc.getMessage(), exc, true, ps);
                } else if (exc instanceof RuntimeException) {
                    throw (RuntimeException) exc;
                } else {
                    throw new RuntimeException("Unexpected exception", exc);
                }
            };

    private final String name;
    private final String info;
    private final String invocationArgs;
    private final int minimalArgsCount;
    private final int maximalArgsCount;

    /**
     * @param invocationArgs
     *         Sequence of arguments that can be mentioned, e.g. ' {@code <key> <value>}'
     * @param info
     *         Short description of command.
     */
    public AbstractCommand(String name,
                           String invocationArgs,
                           String info,
                           int minimalArgsCount,
                           int maximalArgsCount) {
        Objects.requireNonNull(name, "Name must not be null");

        this.name = name;
        this.info = info;
        this.invocationArgs = invocationArgs;
        this.minimalArgsCount = minimalArgsCount;
        this.maximalArgsCount = maximalArgsCount;
    }

    public AbstractCommand(String name, String invocationArgs, String info, int expectedArgsCount) {
        this(name, invocationArgs, info, expectedArgsCount, expectedArgsCount);
    }

    public int getMinimalArgsCount() {
        return minimalArgsCount;
    }

    public int getMaximalArgsCount() {
        return maximalArgsCount;
    }

    /**
     * In implementation of {@link ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell
     * .AbstractCommand}
     * arguments number is checked first and then
     * {@link #executeSafely(State, String[])} is invoked.<br/>
     * If you want to disable forced arguments number checking, override this method without invocation super
     * method and put empty implementation inside {@link #executeSafely(State, String[])}.
     */
    @Override
    public void execute(final State state, final String[] args) throws TerminalException {
        performAccurately(
                () -> {
                    checkArgsNumber(args, minimalArgsCount, maximalArgsCount);
                    executeSafely(state, args);
                }, DATABASE_ERROR_HANDLER, state.getOutputStream());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public String getInvocation() {
        return invocationArgs;
    }

    protected abstract void executeSafely(State state, String[] args) throws
                                                                      IllegalArgumentException,
                                                                      NoActiveTableException,
                                                                      IllegalStateException,
                                                                      NullPointerException,
                                                                      InvocationException,
                                                                      ParseException,
                                                                      IOException,
                                                                      ExecutionNotPermittedException,
                                                                      UnexpectedRemoteException;

    void checkArgsNumber(String[] args, int minimal, int maximal) throws WrongArgsNumberException {
        if (args.length < minimal || args.length > maximal) {
            throw new WrongArgsNumberException(this, args[0]);
        }
    }
}
