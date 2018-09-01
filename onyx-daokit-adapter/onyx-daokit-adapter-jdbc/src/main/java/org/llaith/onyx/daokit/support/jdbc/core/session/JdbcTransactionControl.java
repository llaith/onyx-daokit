package org.llaith.onyx.daokit.support.jdbc.core.session;


import org.llaith.onyx.toolkit.session.SessionControl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
public class JdbcTransactionControl extends SessionControl<Connection> {

    public static class FactoryBuilder {

        final Map<Class<?>,Function<Connection,?>> factories = new HashMap<>();

        public <X> JdbcTransactionControl.FactoryBuilder addFactory(final Class<X> klass, Function<Connection,X> fn) {

            this.factories.put(klass, fn);

            return this;

        }
    }

    public JdbcTransactionControl(final DataSource ds, final JdbcTransactionControl.FactoryBuilder builder) {
        this(ds, builder.factories);
    }

    public JdbcTransactionControl(final DataSource ds, final Map<Class<?>,Function<Connection,?>> factories) {
        super(new JdbcTransactionProvider(ds), factories);
    }

}
