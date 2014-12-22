package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.transactions.Transaction;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.servlet.transactions.TransactionPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpDBServer {
    public static final String PARAM_TABLE = "table";
    public static final String PARAM_TRANSACTION_ID = "tid";
    public static final String FIELD_DIFF = "diff";
    public static final String PARAM_KEY = "key";
    public static final String PARAM_VALUE = "value";
    private static final int TRANSACTION_ID_UPPER_BOUND = 100000;
    /**
     * Default value: 10 minutes.
     */
    private static final long TRANSACTION_TIME_TO_LIVE = 10 * 60 * 1000L;
    private final TableProvider localProvider;
    private Server httpServer;
    private volatile TransactionPool transactionPool;

    public HttpDBServer(TableProvider localProvider) {
        this.localProvider = localProvider;
    }

    public void startHttpServer(String host, int port) throws Exception {
        if (isStarted()) {
            throw new IllegalStateException("HttpServer is already initialized");
        }

        transactionPool = new TransactionPool(TRANSACTION_ID_UPPER_BOUND, TRANSACTION_TIME_TO_LIVE);

        httpServer = new Server(new InetSocketAddress(host, port));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

        // Adding servlets.
        handler.addServlet(new ServletHolder(new BeginServlet()), "/begin");
        handler.addServlet(new ServletHolder(new CommitServlet()), "/commit");
        handler.addServlet(new ServletHolder(new RollbackServlet()), "/rollback");
        handler.addServlet(new ServletHolder(new GetServlet()), "/get");
        handler.addServlet(new ServletHolder(new PutServlet()), "/put");
        handler.addServlet(new ServletHolder(new SizeServlet()), "/size");

        httpServer.setHandler(handler);

        try {
            httpServer.start();
        } catch (Exception exc) {
            transactionPool.closePool();
            transactionPool = null;
            httpServer = null;
            throw exc;
        }
    }

    public void stopHttpServerIfStarted() throws Exception {
        if (isStarted()) {
            stopHttpServer();
        }
    }

    public void stopHttpServer() throws Exception {
        if (!isStarted()) {
            throw new IllegalStateException("HttpServer not initialized");
        }

        try {
            httpServer.stop();
        } finally {
            transactionPool.closePool();
            httpServer = null;
            transactionPool = null;
        }
    }

    public boolean isStarted() {
        return httpServer != null;
    }

    class BadRequestException extends Exception {
        public BadRequestException(String message) {
            super(message);
        }
    }

    abstract class BaseDBServlet extends HttpServlet {
        protected abstract void serveGet(HttpServletRequest request, HttpServletResponse response)
                throws Exception;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            try {
                serveGet(req, resp);
            } catch (BadRequestException exc) {
                resp.reset();
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println(exc.getMessage());
            } catch (Exception exc) {
                resp.reset();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println(exc.getMessage());
            }
        }
    }

    class SizeServlet extends BaseDBServlet {
        @Override
        protected void serveGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
            int transactionID = Integer.parseInt(request.getParameter(PARAM_TRANSACTION_ID));
            Transaction<Table> transaction = transactionPool.obtainTransaction(transactionID);
            try {
                int size = transaction.getExtraData().size();
                response.getWriter().print(size + "");
            } finally {
                transactionPool.releaseTransaction(transaction);
            }
        }
    }

    class PutServlet extends BaseDBServlet {
        @Override
        protected void serveGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
            int transactionID = Integer.parseInt(request.getParameter(PARAM_TRANSACTION_ID));
            Transaction<Table> transaction = transactionPool.obtainTransaction(transactionID);
            try {
                String key = request.getParameter(PARAM_KEY);
                String serializedNewValue = request.getParameter(PARAM_VALUE);

                // Deserializing new value.
                Storeable storeableNewValue =
                        localProvider.deserialize(transaction.getExtraData(), serializedNewValue);

                // Putting new value instead of old.
                Storeable storeableOldValue = transaction.getExtraData().put(key, storeableNewValue);

                // This key did not exist before.
                if (storeableOldValue == null) {
                    throw new BadRequestException("Key not found: " + key);
                }
                // Now we must serialize old value.
                String serializedOldValue =
                        localProvider.serialize(transaction.getExtraData(), storeableOldValue);
                response.getWriter().print(serializedOldValue);
            } finally {
                transactionPool.releaseTransaction(transaction);
            }
        }
    }

    class GetServlet extends BaseDBServlet {
        @Override
        protected void serveGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
            int transactionID = Integer.parseInt(request.getParameter(PARAM_TRANSACTION_ID));
            Transaction<Table> transaction = transactionPool.obtainTransaction(transactionID);
            try {
                String key = request.getParameter(PARAM_KEY);
                Storeable storeableValue = transaction.getExtraData().get(key);
                // Not found
                if (storeableValue == null) {
                    throw new BadRequestException("Key not found: " + key);
                }
                // Now we must serialize it.
                String serializedValue = localProvider.serialize(transaction.getExtraData(), storeableValue);
                response.getWriter().print(serializedValue);
            } finally {
                transactionPool.releaseTransaction(transaction);
            }
        }
    }

    class CommitServlet extends BaseDBServlet {
        @Override
        protected void serveGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
            int transactionID = Integer.parseInt(request.getParameter(PARAM_TRANSACTION_ID));
            Transaction<Table> transaction = transactionPool.obtainTransaction(transactionID);
            try {
                int diff = transaction.getExtraData().commit();
                response.getWriter().print(String.format("%s=%d", FIELD_DIFF, diff));
            } finally {
                transactionPool.killTransaction(transaction);
            }
        }
    }

    class RollbackServlet extends BaseDBServlet {
        @Override
        protected void serveGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
            int transactionID = Integer.parseInt(request.getParameter(PARAM_TRANSACTION_ID));
            Transaction<Table> transaction = transactionPool.obtainTransaction(transactionID);
            try {
                int diff = transaction.getExtraData().rollback();
                response.getWriter().print(String.format("%s=%d", FIELD_DIFF, diff));
            } finally {
                transactionPool.killTransaction(transaction);
            }
        }
    }

    class BeginServlet extends BaseDBServlet {
        @Override
        protected void serveGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
            String tableName = request.getParameter(PARAM_TABLE);

            // exception can occur here
            Table table = localProvider.getTable(tableName);
            if (table == null) {
                throw new IllegalArgumentException("Table " + tableName + " not exists");
            }

            Transaction<Table> transaction = transactionPool.newTransaction();
            try {
                transaction.setExtraData(table);
                int transactionID = transaction.getTransactionID();
                response.getWriter().print(String.format("%s=%05d", PARAM_TRANSACTION_ID, transactionID));
            } finally {
                transactionPool.releaseTransaction(transaction);
            }
        }

    }
}
