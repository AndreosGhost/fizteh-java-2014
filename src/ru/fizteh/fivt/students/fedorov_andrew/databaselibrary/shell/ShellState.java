package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExitRequest;

import java.io.PrintStream;

/**
 * Base interface that encapsulates all data and commands to work with.
 * @param <S>
 *         Implementation of this interface
 */
public interface ShellState<S extends ShellState<S>> extends CommandContainer<S> {
    /**
     * Terminal output stream for printing messages.
     */
    PrintStream getOutputStream();

    /**
     * Performs clean up after all work is done and the shell is going to exit.
     */
    void cleanup();

    /**
     * Makes a greeting string that can be printed.
     */
    String getGreetingString();

    /**
     * Performs all initialization work. Called when shell is starting.
     * @param host
     *         host shell that will work with this ShellState object.
     */
    void init(Shell<S> host) throws Exception;

    /**
     * Safely exit with cleanup.
     * @throws ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.ExitRequest
     *         you must throw this exception to indicate that you really want to exit. Do no call
     *         {@link System#exit(int)} instead of it.
     */
    void prepareToExit(int exitCode) throws ExitRequest;
}
