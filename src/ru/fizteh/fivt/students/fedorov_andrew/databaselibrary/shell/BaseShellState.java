package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Base class for states with single init possibility.
 * @param <State>
 */
public abstract class BaseShellState<State extends ShellState<State>> implements ShellState<State> {
    protected Shell<State> host;

    protected void checkInitialized() {
        Objects.requireNonNull(host, "Not initialized");
    }

    @Override
    public PrintStream getOutputStream() {
        checkInitialized();
        return host.getOutputStream();
    }

    @Override
    public String getGreetingString() {
        return "$ ";
    }

    @Override
    public void init(Shell<State> host) throws Exception {
        Objects.requireNonNull(host, "Host shell must not be null");

        if (this.host != null) {
            throw new IllegalStateException("Already initialized");
        }

        this.host = host;
    }

}
