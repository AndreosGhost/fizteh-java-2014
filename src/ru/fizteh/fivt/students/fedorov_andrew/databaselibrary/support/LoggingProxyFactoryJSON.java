package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.json.JSONComplexObject;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.json.JSONField;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.json.JSONMaker;

/**
 * Extension which writes logs in JSON format using {@link ru.fizteh.fivt.students.fedorov_andrew
 * .databaselibrary.json.JSONMaker}
 */
public class LoggingProxyFactoryJSON extends LoggingProxyFactoryBase {

    @Override
    protected String constructReport(long timestamp,
                                     String invokedClass,
                                     String invokedMethod,
                                     Object[] arguments,
                                     Throwable thrown,
                                     Object returnedValue,
                                     boolean isVoid) {
        Object report;
        if (thrown != null) {
            report = new LoggingReportWithThrown(timestamp, invokedClass, invokedMethod, arguments, thrown);
        } else if (isVoid) {
            report = new LoggingReport(timestamp, invokedClass, invokedMethod, arguments);
        } else {
            report = new LoggingReportWithReturnValue(
                    timestamp, invokedClass, invokedMethod, arguments, returnedValue);
        }
        return JSONMaker.makeJSON(report);
    }

    @JSONComplexObject
    private static class LoggingReport {
        @JSONField
        final long timestamp;

        @JSONField(name = "class")
        final String invokeeClass;

        @JSONField(name = "method")
        final String invokeeMethod;

        @JSONField
        final Object[] arguments;

        public LoggingReport(long timestamp, String invokeeClass, String invokeeMethod, Object[] arguments) {
            this.timestamp = timestamp;
            this.invokeeClass = invokeeClass;
            this.invokeeMethod = invokeeMethod;
            this.arguments = arguments;
        }
    }

    @JSONComplexObject
    private static class LoggingReportWithReturnValue extends LoggingReport {
        @JSONField
        final Object returnValue;

        public LoggingReportWithReturnValue(long timestamp,
                                            String invokeeClass,
                                            String invokeeMethod,
                                            Object[] arguments,
                                            Object returnValue) {
            super(timestamp, invokeeClass, invokeeMethod, arguments);
            this.returnValue = returnValue;
        }
    }

    @JSONComplexObject
    private static class LoggingReportWithThrown extends LoggingReport {
        @JSONField
        final Throwable thrown;

        public LoggingReportWithThrown(long timestamp,
                                       String invokeeClass,
                                       String invokeeMethod,
                                       Object[] arguments,
                                       Throwable thrown) {
            super(timestamp, invokeeClass, invokeeMethod, arguments);
            this.thrown = thrown;
        }
    }
}
