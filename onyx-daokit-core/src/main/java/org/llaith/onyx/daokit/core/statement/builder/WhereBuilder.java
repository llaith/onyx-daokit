package org.llaith.onyx.daokit.core.statement.builder;

import com.google.common.base.Joiner;
import org.llaith.onyx.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.toolkit.exception.UncheckedException;
import org.llaith.onyx.toolkit.lang.Guard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO http://www.w3schools.com/sql/sql_where.asp
 */
public interface WhereBuilder {

    WhereBuilder addClause(String fragment);

    WhereBuilder addAndClauseList(Collection<String> columns);

    WhereBuilder ifNullAddClause(Object o, String fragment);

    WhereBuilder ifNotNullAddClause(Object o, String fragment);

    interface TypeEval {

        String toString(String col, String param, Object o);

    }

    class Impl implements WhereBuilder {

        private static final Joiner spaceJoiner = Joiner.on(" ");

        private final List<String> fragments = new ArrayList<>();

        private final String whereKey;

        private final StatementBuilder statementBuilder;

        public Impl(final StatementBuilder statementBuilder, final String whereKey) {

            this.statementBuilder = Guard.notNull(statementBuilder);

            this.whereKey = whereKey;

        }

        @Override
        public WhereBuilder ifNullAddClause(final Object o, final String fragment) {

            return (o != null) ?
                    this :
                    addClause(fragment);

        }

        @Override
        public WhereBuilder ifNotNullAddClause(final Object o, final String fragment) {

            return (o == null) ?
                    this :
                    addClause(fragment);

        }

        @Override
        public WhereBuilder addClause(final String fragment) {

            this.fragments.add(fragment);

            return this;

        }
        @Override
        public WhereBuilder addAndClauseList(final Collection<String> columns) {

            for (String c : columns)
                this.addClause(String.format("AND %s = %s", c, StatementBuilder.prefix(c)));

            return this;

        }

        public void configureStatement() {

            if (this.fragments.isEmpty()) throw new UncheckedException("Cannot use an empty where-builder.");

            statementBuilder.resolveAsText(whereKey, spaceJoiner.join(this.fragments));

        }

    }

}
