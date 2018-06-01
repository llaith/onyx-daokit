package org.llaith.obsidian.daokit.core.statement;

import org.llaith.onyx.toolkit.util.exception.UncheckedException;
import org.llaith.onyx.toolkit.util.lang.Guard;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 */
public class Statement {

    public static Statement of(final String name, final String sql) {

        return new Statement(name, sql);

    }

    private final String name;

    private final String statement;

    private final LinkedHashSet<String> params = new LinkedHashSet<>();

    public Statement(final String name, final String statement) {

        this(name, statement, Collections.emptySet());

    }

    public Statement(final String name, final String statement, final Set<String> params) {

        if (statement.contains(StatementBuilder.INTERNAL_PREFIX))
            throw new UncheckedException("Parameter replacement failed.");

        this.name = Guard.notNull(name);

        this.statement = Guard.notNull(statement);

        if (params != null) this.params.addAll(params);

    }

    public String name() {
        return name;
    }

    public String statement() {
        return statement;
    }

    public Collection<String> params() {

        return Collections.unmodifiableSet(this.params);

    }

}
