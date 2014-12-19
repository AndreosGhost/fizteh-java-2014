package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.shell;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.exception.TerminalException;

/**
 * This class represents an executable shell command
 * @author phoenix
 */
public interface Command<State extends ShellState<State>> {
    void execute(State state, String[] args) throws TerminalException;

    /**
     * Name this command can be invoked by.
     */
    String getName();

    /**
     * Information text for the command.
     */
    String getInfo();

    /**
     * Complete formula for command invocation excluding command name.
     */
    String getInvocation();

    default String buildHelpLine() {
        return String.format(
                "\t%s%s\t%s", getName(), getInvocation() == null ? "" : (' ' + getInvocation()), getInfo());
    }
}
