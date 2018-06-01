/*
 * Copyright (c) 2016.
 */

package org.llaith.obsidian.daokit.support.jdbi.core.session;

import org.llaith.onyx.toolkit.session.Session;
import org.llaith.onyx.toolkit.session.SessionControl;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;

import java.util.Map;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class JdbiTransactionProvider implements SessionControl.SessionProvider<Handle> {

    private final DBI jdbi;

    public JdbiTransactionProvider(final DBI jdbi) {

        this.jdbi = jdbi;

    }

    @SuppressWarnings("squid:S1166") // rethrowing the getCause() is by design!
    @Override
    public <X> X newSession(final Map<Class<?>,Function<Handle,?>> factories, final SessionControl.SessionCallback<Handle,X> callback) {

        try {

            return jdbi.inTransaction((conn, status) -> {

                try {

                    return callback.in(new Session<>(
                            factories,
                            conn));

                } catch (RuntimeException ex) {

                    status.setRollbackOnly();

                    throw ex;

                }

            });

        } catch (CallbackFailedException ex) {

            throw UncheckedException.wrap(ex.getCause());

        }

    }

}
