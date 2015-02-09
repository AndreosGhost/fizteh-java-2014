package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.junit.Before;
import ru.fizteh.fivt.proxy.LoggingProxyFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test.support.BAOSDuplicator;

import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class LoggingProxyFactoryTestBase {
    protected static final String TIMESTAMP = "timestamp";
    protected static final String CLASS = "class";
    protected static final String METHOD = "method";
    protected static final String ARGUMENTS = "arguments";
    protected static final String THROWN = "thrown";
    protected static final String RETURN_VALUE = "returnValue";
    protected final BAOSDuplicator out = new BAOSDuplicator(System.out);
    protected final Writer writer = new OutputStreamWriter(out);
    protected LoggingProxyFactory factory;
    protected TestFace wrapped;

    protected String getOutput() {
        return out.toString();
    }

    @Before
    public void prepare() {
        out.reset();
        factory = obtainFreshFactory();
        wrapped = (TestFace) factory.wrap(writer, new TestFaceImpl(), TestFace.class);
    }

    protected abstract LoggingProxyFactory obtainFreshFactory();

    protected interface TestFace {
        default void doNothing() {
        }

        default boolean boolMethod(int a, Integer b) {
            return true;
        }

        default <T> String throwingMethod(Iterable<T> iterable) throws Exception {
            throw new Exception();
        }
    }

    protected static class TestFaceImpl implements TestFace {}
}
