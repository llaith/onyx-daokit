/*
 * Copyright (c) 2016.
 */

package org.llaith.onyx.daokit.support.jdbc.core.session;

import org.llaith.onyx.toolkit.pattern.session.Session;
import org.llaith.onyx.toolkit.pattern.session.SessionControl;
import org.llaith.onyx.toolkit.pattern.session.SessionControl.SessionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by nos on 23/06/15.
 * <p>
 * Completely untested as of yet. I plan to use this one, and a JPA/Hibernate one, as potential transitional steps
 * away from the existing methods in use towards the simplified hikari/jdbi approach.
 */
public class JdbcTransactionProvider implements SessionProvider<Connection> {

    private static final Logger logger = LoggerFactory.getLogger(JdbcTransactionProvider.class);

    private final DataSource ds;

    public JdbcTransactionProvider(final DataSource ds) {

        this.ds = ds;

    }

    @Override
    public <X> X newSession(final Map<Class<?>,Function<Connection,?>> factories, final SessionControl.SessionCallback<Connection,X> callback) {

        Connection connection = null;

        try {

            connection = Objects.requireNonNull(ds.getConnection());

            if (!connection.getAutoCommit()) {

                // make sure there is no current transaction
                connection.rollback();

            }

            // start a new transaction
            connection.setAutoCommit(false);

            return makeCall(connection, factories, callback);

        } catch (SQLException ex) {

            throw new RuntimeException("Could not prepare connection for use.", ex);

        } finally {

            if (connection != null) {

                try {connection.setAutoCommit(true);} catch (SQLException ex) {
                    logger.error("Could not reset connection status after use.", ex);
                }

                try {connection.close();} catch (SQLException ex) {
                    logger.error("Could not close connection after use. Connection may have leaked.", ex);
                }

            }

        }

    }

    private <X> X makeCall(
            final Connection connection,
            final Map<Class<?>,Function<Connection,?>> factories,
            final SessionControl.SessionCallback<Connection,X> callback) {

        try {
            // user must close the statement manually in a finally block of the callback.
            final X result = callback.in(new Session<>(
                    factories,
                    connection));

            // commit the transaction
            connection.commit();

            // and return
            return result;

        } catch (Exception ex) {

            try {connection.rollback();} catch (SQLException ex2) {
                // this is potentially serious. We can hope that the driver implements rollback on connection close.
                final RuntimeException exNew = new RuntimeException("Could not execute statememt, nor rollback.", ex);
                exNew.addSuppressed(ex2);
                throw exNew;
            }

            throw new RuntimeException("Could not execute statememt.", ex);

        }

    }

}
