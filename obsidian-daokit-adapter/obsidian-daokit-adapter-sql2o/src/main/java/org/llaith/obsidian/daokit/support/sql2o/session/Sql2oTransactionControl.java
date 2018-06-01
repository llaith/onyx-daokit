/*
 * Copyright (c) 2016.
 */

package org.llaith.obsidian.daokit.support.sql2o.session;

import org.llaith.onyx.toolkit.session.SessionControl;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Must be a subclass for injection
 */
public class Sql2oTransactionControl extends SessionControl<Connection> {

    public static class FactoryBuilder {

        final Map<Class<?>,Function<Connection,?>> factories = new HashMap<>();

        public <X> FactoryBuilder addFactory(final Class<X> klass, Function<Connection,X> fn) {

            this.factories.put(klass, fn);

            return this;

        }
        
    }

    public Sql2oTransactionControl(final Sql2o sql2o, final FactoryBuilder builder) {
        
        this(sql2o, builder.factories);
        
    }

    public Sql2oTransactionControl(final Sql2o sql2o, final Map<Class<?>,Function<Connection,?>> factories) {

        super(new Sql2oTransactionProvider(sql2o), factories);

    }

}
