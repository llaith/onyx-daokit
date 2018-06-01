/*
 * Copyright (c) 2016.
 */

package org.llaith.obsidian.daokit.support.sql2o.session;

import org.llaith.onyx.toolkit.session.Session;
import org.llaith.onyx.toolkit.session.SessionControl;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Map;
import java.util.function.Function;

/**
 * Must be a subclass for injection
 */
public class Sql2oTransactionProvider implements SessionControl.SessionProvider<Connection> {

    private final Sql2o sql2o;

    public Sql2oTransactionProvider(final Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public <X> X newSession(final Map<Class<?>,Function<Connection,?>> factories, final SessionControl.SessionCallback<Connection,X> callback) {

        try (final Connection connection = this.sql2o.beginTransaction()) {

            final X result = callback.in(new Session<>(
                    factories,
                    connection));

            connection.commit();

            return result;

        }

    }

}
