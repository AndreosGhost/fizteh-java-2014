package ru.fizteh.fivt.students.fedorov_andrew.filemap;

public interface Command {
    public void execute(Shell shell, String[] args) throws HandledException;

    public String getInfo();

    public String getInvocation();
}
