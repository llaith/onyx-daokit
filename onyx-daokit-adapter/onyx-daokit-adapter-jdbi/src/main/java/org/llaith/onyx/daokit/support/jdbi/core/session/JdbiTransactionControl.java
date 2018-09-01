package org.llaith.onyx.daokit.support.jdbi.core.session;

import org.llaith.onyx.toolkit.session.SessionControl;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
public class JdbiTransactionControl extends SessionControl<Handle> {

    public static class FactoryBuilder {

        final Map<Class<?>,Function<Handle,?>> factories = new HashMap<>();

        public <X> FactoryBuilder addFactory(final Class<X> klass, Function<Handle,X> fn) {

            this.factories.put(klass, fn);

            return this;

        }
    }

    public JdbiTransactionControl(final DBI jdbi, final FactoryBuilder builder) {
        this(jdbi, builder.factories);
    }

    public JdbiTransactionControl(final DBI jdbi, final Map<Class<?>,Function<Handle,?>> factories) {
        super(new JdbiTransactionProvider(jdbi), factories);
    }

}
