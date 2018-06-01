package org.llaith.obsidian.daokit.core.statement;

import com.google.common.collect.ImmutableMap;
import org.llaith.obsidian.daokit.core.statement.annotation.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.llaith.onyx.toolkit.util.exception.CompoundExceptionHandler;
import org.llaith.onyx.toolkit.util.exception.ExceptionHandler;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;
import org.llaith.onyx.toolkit.util.lang.Guard;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 *
 */
public class ComposedStatementBuilder {

    public static ComposedStatementBuilder from(final Statement statement) {

        return new ComposedStatementBuilder(statement);

    }

    private static final Logger logger = LoggerFactory.getLogger(ComposedStatementBuilder.class);

    // it's not expected we need to configure this
    private static final BiFunction<Field,Object,Object> reader = new PropertyUtil.DefaultReader();

    private final String statement;

    private final CompoundExceptionHandler<RuntimeException> exceptionHandler = new CompoundExceptionHandler<>();

    private final List<String> params = new ArrayList<>();

    private Map<String,Serializable> values = new HashMap<>();

    private Set<String> check;
    private Set<String> missing;

    public ComposedStatementBuilder(final Statement statement) {

        this.statement = Guard.notNull(statement).statement();

        this.params.addAll(Guard.notNull(statement.params()));

        this.check = new HashSet<>(params);

        this.missing = new HashSet<>(params);

    }

    public ComposedStatementBuilder addExceptionHandler(final ExceptionHandler<RuntimeException> handler) {

        this.exceptionHandler.addHandler(handler);

        return this;

    }

    public ComposedStatementBuilder setParameter(final String name, final Serializable value) {
        this.addParam(name, Guard.notNull(value));
        return this;
    }

    public ComposedStatementBuilder setParameters(final Map<String,Serializable> map) {
        return this.setParameters(map, true);
    }

    public ComposedStatementBuilder setParameters(final ImmutableMap.Builder<String,Serializable> builder) {

        return this.setParameters(Guard.notNull(builder.build()), true);

    }

    public ComposedStatementBuilder setParametersFrom(final Object entity) {

        return this.setParameters(
                PropertyUtil.pullSerializablePropertyValuesFrom(
                        Guard.notNull(entity),
                        reader,
                        new HashMap<>()),
                false);

    }

    private ComposedStatementBuilder setParameters(final Map<String,Serializable> map, final boolean strict) {
        for (final String name : this.params) {
            try {
                this.addParam(name, map.get(name));
            } catch (UncheckedException e) {
                if (strict) throw e;
            }
        }
        return this;
    }

    private void addParam(final String name, final Serializable value) {

        if (!this.check.contains(Guard.notBlankOrNull(name)))
            throw new UncheckedException("Unknown parameter: " + name);

        if (!this.missing.remove(name) && logger.isDebugEnabled()) logger.debug(String.format(
                "Overriding param %s, old value %s, new value %s.",
                name,
                this.values.get(name),
                value));

        this.values.put(name, value);

    }

    public <C> ComposedStatement<C> usingConnection(final C connection) {

        if (!this.missing.isEmpty()) throw new UncheckedException("Missing params: " + this.missing);

        return new ComposedStatement<>(
                this.exceptionHandler,
                connection,
                this.statement,
                this.params,
                this.values);

    }

}
