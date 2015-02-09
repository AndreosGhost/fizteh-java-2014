package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml.XMLComplexObject;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml.XMLField;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml.XMLMaker;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Extension which writes logs in XML format using {@link ru.fizteh.fivt.students.fedorov_andrew
 * .databaselibrary.xml.XMLMaker}.
 */
public class LoggingProxyFactoryXML extends LoggingProxyFactoryBase {

    @Override
    protected String constructReport(long timestamp,
                                     String invokedClass,
                                     String invokedMethod,
                                     Object[] arguments,
                                     Throwable thrown,
                                     Object returnedValue,
                                     boolean isVoid) {
        Object report =
                new LoggingReport(timestamp, invokedClass, invokedMethod, arguments, returnedValue, thrown);
        return XMLMaker.makeXML(report, "invoke");
    }

    @XMLComplexObject(wrapper = true)
    private static class Argument {
        @XMLField
        private final Object argument;

        public Argument(Object argument) {
            this.argument = argument;
        }

        public static Argument[] wrapObjects(Object... arguments) {
            if (arguments == null) {
                return null;
            }
            return Arrays.stream(arguments).map(Argument::new).collect(Collectors.toList())
                         .toArray(new Argument[arguments.length]);
        }
    }

    @XMLComplexObject
    private static class LoggingReport {
        @XMLField(inline = true)
        final long timestamp;

        @XMLField(name = "class", inline = true)
        final String invokeeClass;

        @XMLField(name = "name", inline = true)
        final String invokeeMethod;

        @XMLField(childName = "argument", nullPolicy = XMLField.NULLPOLICY_EMPTY_IF_NULL)
        final Argument[] arguments;

        @XMLField(nullPolicy = XMLField.NULLPOLICY_IGNORE_IF_NULL)
        final Throwable thrown;

        @XMLField(name = "return", nullPolicy = XMLField.NULLPOLICY_IGNORE_IF_NULL)
        final Object returnValue;

        public LoggingReport(long timestamp,
                             String invokeeClass,
                             String invokeeMethod,
                             Object[] arguments,
                             Object returnValue,
                             Throwable thrown) {
            this.timestamp = timestamp;
            this.invokeeClass = invokeeClass;
            this.invokeeMethod = invokeeMethod;
            this.arguments = Argument.wrapObjects(arguments);
            this.returnValue = returnValue;
            this.thrown = thrown;
        }
    }

}
