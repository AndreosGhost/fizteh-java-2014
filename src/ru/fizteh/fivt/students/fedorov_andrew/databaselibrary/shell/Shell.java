package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExitRequest;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Log;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Utility;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that represents a terminal which can execute some commands that work with some data.
 * @param <ShellStateImpl>
 *         Concrete implementation of {@link ru.fizteh.fivt.students.fedorov_andrew
 *         .databaselibrary.shell.ShellState}
 *         the shell works with.
 * @author phoenix
 * @see Command
 * @see ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell.ShellState
 */
public class Shell<ShellStateImpl extends ShellState<ShellStateImpl>> {
    private static final char QUOTE_CHARACTER = '\"';
    private static final char ESCAPE_CHARACTER = '\\';

    private static final char COMMAND_END_CHARACTER = ';';

    private static final int READ_BUFFER_SIZE = 16 * 1024;
    /**
     * Object encapsulating commands and data they work with.
     */
    private final ShellStateImpl shellState;
    private final PrintStream outputStream;
    private boolean valid = true;

    /**
     * If the user is entering commands or it is package mode.
     */
    private boolean interactive;

    public Shell(ShellStateImpl shellState, OutputStream outputStream) throws TerminalException {
        this.shellState = shellState;
        this.outputStream = outputStream instanceof PrintStream
                            ? (PrintStream) outputStream
                            : new PrintStream(outputStream);
        init();
    }

    public Shell(ShellStateImpl shellState) throws TerminalException {
        this(shellState, System.out);
    }

    /**
     * Handles an occurred exception.
     * @param cause
     *         occurred exception. If null, an {@link Exception} is constructed via {@link
     *         Exception#Exception(String)}.
     * @param message
     *         message that can be reported to user and is written to log.
     * @param reportToUser
     *         if true, message is printed to errorStream.
     */
    public static void handleError(String message,
                                   Throwable cause,
                                   boolean reportToUser,
                                   PrintStream errorStream) throws TerminalException {
        if (reportToUser) {
            errorStream.println(message == null ? cause.getMessage() : message);
        }
        Log.log(SingleDBCommands.class, cause, message);
        if (cause == null) {
            throw new TerminalException(message);
        } else {
            throw new TerminalException(message, cause);
        }
    }

    /**
     * Splits command string into commands.
     * @param commandsStr
     *         Commands split by {@link #COMMAND_END_CHARACTER}.
     * @return List of commands, each command is an array of its parts (space splitters are excluded from
     * everywhere except quoted parts).
     * @throws java.text.ParseException
     *         In case of bad format.
     */
    public static List<String[]> splitCommandsString(String commandsStr) throws ParseException {
        // command1 option1,  "string const1", ...; command2 ...

        if (!commandsStr.endsWith(COMMAND_END_CHARACTER + "")) {
            commandsStr += COMMAND_END_CHARACTER;
        }

        List<String[]> commands = new LinkedList<>();
        List<String> commandParts = new LinkedList<>();

        for (int start = 0, index = 0, len = commandsStr.length(); index < len; ) {
            char symbol = commandsStr.charAt(index);

            if (symbol == QUOTE_CHARACTER) {
                index = Utility.findClosingQuotes(
                        commandsStr, index + 1, len, QUOTE_CHARACTER, ESCAPE_CHARACTER);
                if (index < 0) {
                    throw new ParseException("Cannot find closing quotes", -1);
                }
                index++;
            } else if (Character.isSpaceChar(symbol) || symbol == COMMAND_END_CHARACTER) {
                String part = commandsStr.substring(start, index).trim();
                if (!part.isEmpty()) {
                    commandParts.add(part);
                }
                start = index + 1;
                index++;

                if (symbol == COMMAND_END_CHARACTER) {
                    commands.add(commandParts.toArray(new String[commandParts.size()]));
                    commandParts = new LinkedList<>();
                }
            } else {
                index++;
            }
        }

        return commands;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    /**
     * Executes command in this shell
     * @param args
     *         some shell command
     * @throws ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException
     */
    private void execute(String[] args) throws TerminalException, ExitRequest {
        if (args.length == 0) {
            return;
        }

        Log.log(Shell.class, "Invocation request: " + Arrays.toString(args));

        Command<ShellStateImpl> command = shellState.getCommands().get(args[0]);
        if (command == null) {
            handleError(args[0] + ": command is missing", null, true, getOutputStream());
        } else {
            try {
                command.execute(shellState, args);
            } catch (TerminalException | ExitRequest exc) {
                // If it is TerminalException, error report is already written.
                throw exc;
            } catch (Exception exc) {
                handleError(args[0] + ": Method execution error", exc, true, getOutputStream());
            }
        }
    }

    /**
     * Prepares shell for further command interpretation
     */
    private void init() throws TerminalException {
        Log.log(Shell.class, "Shell starting");

        try {
            shellState.init(this);
        } catch (Exception exc) {
            handleError(exc.getMessage(), exc, true, getOutputStream());
        }
    }

    public boolean isInteractive() {
        return interactive;
    }

    private void checkValid() throws IllegalStateException {
        if (!valid) {
            throw new IllegalStateException("Shell has already run");
        }
    }

    /**
     * Execute commands from input stream. Commands are awaited till the-end-of-stream.
     */
    private int run(InputStream stream, boolean interactive) throws TerminalException {
        checkValid();
        valid = false;
        this.interactive = interactive;

        if (stream == null) {
            throw new IllegalArgumentException("Input stream must not be null");
        }

        boolean exitRequested = false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream), READ_BUFFER_SIZE)) {
            while (true) {
                if (interactive) {
                    outputStream.print(shellState.getGreetingString());
                }
                String str = reader.readLine();

                // End of stream.
                if (str == null) {
                    break;
                }

                try {
                    try {
                        List<String[]> commands = splitCommandsString(str);
                        for (String[] command : commands) {
                            execute(command);
                        }
                    } catch (ParseException exc) {
                        handleError("Failed to parse: " + exc.getMessage(), exc, true, getOutputStream());
                    }
                } catch (TerminalException exc) {
                    // Exception is already handled.
                    if (!interactive) {
                        exitRequested = true;
                        shellState.prepareToExit(1);
                    }
                }
            }
        } catch (IOException exc) {
            exitRequested = true;
            handleError("Error in input stream: " + exc.getMessage(), exc, true, getOutputStream());
            // No need to cleanup - work has not been started.
        } catch (ExitRequest request) {
            exitRequested = true;
            return request.getCode();
        } finally {
            if (!exitRequested) {
                try {
                    shellState.prepareToExit(0);
                } catch (ExitRequest request) {
                    return request.getCode();
                }
            }
        }

        // If all contracts are honoured, this line is unreachable.
        throw new AssertionError("No exit request performed");
    }

    public int run(InputStream inputStream) throws TerminalException {
        return run(inputStream, true);
    }

    /**
     * Execute commands from command line arguments. Note that command line arguments are first
     * concatenated into a single line then split and parsed.
     * @param args
     *         Array of commands. If an error happens during execution of one of the commands in
     *         the
     *         sequence, next commands will not be executed.
     * @return Exit code. 0 means normal status, anything else - abnormal termination (error).
     */
    public int run(String[] args) throws TerminalException {
        return run(new ByteArrayInputStream(String.join(" ", args).getBytes()), false);
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (valid) {
            try {
                shellState.prepareToExit(0);
            } catch (ExitRequest req) {
                // Ignore it.
            }
        }
    }
}
