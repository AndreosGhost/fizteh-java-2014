package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support;

import ru.fizteh.fivt.proxy.LoggingProxyFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Base class that implements Logging proxy factory. When you extend it you have only to implement method
 * that
 * makes log message from given data.
 */
public abstract class LoggingProxyFactoryBase implements LoggingProxyFactory {
    private volatile boolean loggingEnabled = true;

    /**
     * Constructs log message from the given data.
     * @param timestamp
     *         time when the method was invoked.
     * @param invokedClass
     *         name of the class that contains the invoked method.
     * @param invokedMethod
     *         name of the invoked method.
     * @param arguments
     *         arguments given to the invoked method.
     * @param thrown
     *         exception thrown by the invoked method.
     * @param returnedValue
     *         value returned by the invoked method. Can be null. Is null, if method returns void.
     * @param isVoid
     *         if the invoked method is void.
     * @return message that is written to log.
     */
    protected abstract String constructReport(long timestamp,
                                              String invokedClass,
                                              String invokedMethod,
                                              Object[] arguments,
                                              Throwable thrown,
                                              Object returnedValue,
                                              boolean isVoid);

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    @Override
    public Object wrap(Writer writer, Object implementation, Class<?> interfaceClass) {
        return Proxy.newProxyInstance(
                implementation.getClass().getClassLoader(),
                new Class<?>[] {interfaceClass},
                new Handler(writer, implementation));
    }

    private class Handler implements InvocationHandler {
        private final Writer writer;
        private final Object wrappedObject;

        public Handler(Writer writer, Object wrappedObject) {
            this.writer = writer;
            this.wrappedObject = wrappedObject;
        }

        @Override
        public String toString() {
            return wrappedObject.toString();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isObjectMethod;

            try {
                Object.class.getMethod(method.getName(), method.getParameterTypes());
                isObjectMethod = true;
            } catch (NoSuchMethodException exc) {
                isObjectMethod = false;
            }

            // We do not proxy methods of Object class. We call it on us.
            if (isObjectMethod) {
                try {
                    return method.invoke(this, args);
                } catch (InvocationTargetException exc) {
                    throw exc.getTargetException();
                }
            }

            long timestamp = System.currentTimeMillis();
            Object returnValue = null;
            Throwable thrown = null;

            // We must know it before invocation. Simple reason: suppose the invoked method turns off logging.
            boolean doWriteLog = isLoggingEnabled();

            try {
                boolean accessible = method.isAccessible();
                method.setAccessible(true);
                returnValue = method.invoke(wrappedObject, args);
                method.setAccessible(accessible);
            } catch (InvocationTargetException exc) {
                thrown = exc.getTargetException();
            } catch (IllegalAccessException | IllegalArgumentException exc) {
                Log.log(LoggingProxyFactory.class, exc, "Error on proxy invocation");
            } finally {
                if (doWriteLog) {
                    String report = constructReport(
                            timestamp,
                            wrappedObject.getClass().getSimpleName(),
                            method.getName(),
                            args,
                            thrown,
                            returnValue,
                            void.class.equals(method.getReturnType()));

                    try {
                        writer.write(report);
                        writer.write(System.lineSeparator());
                        writer.flush();
                    } catch (IOException exc) {
                        Log.log(LoggingProxyFactory.class, exc, "Failed to write log report");
                    }
                }
            }

            if (thrown != null) {
                throw thrown;
            } else {
                return returnValue;
            }
        }
    }
}
