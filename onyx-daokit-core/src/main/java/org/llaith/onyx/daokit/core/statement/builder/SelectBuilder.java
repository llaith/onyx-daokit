package org.llaith.onyx.daokit.core.statement.builder;

import com.google.common.base.Joiner;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil;
import org.llaith.onyx.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.toolkit.exception.UncheckedException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.llaith.onyx.toolkit.lang.Guard.notNull;

/**
 * For example, we need to be able to disable property name aliases because jdbi lowercases the sql and
 * as a result, the propertyname will never match.
 */
public interface SelectBuilder {

    SelectBuilder addColumn(String column);

    SelectBuilder addColumn(String alias, String fragment);

    SelectBuilder addColumnsFrom(Class<?> klass);

    SelectBuilder addColumnsFrom(String tableAlias, Class<?> klass);

    SelectBuilder removeColumn(String column);

    class Impl implements SelectBuilder {

        private static final Joiner listJoiner = Joiner.on(", ");

        private final LinkedHashMap<String,String> colsToFragments = new LinkedHashMap<>();

        private final String selectsKey;

        private final StatementBuilder statementBuilder;

        public Impl(final StatementBuilder statementBuilder, final String selectsKey) {

            this.statementBuilder = notNull(statementBuilder);

            this.selectsKey = notNull(selectsKey);

        }

        // used for casts, literals, and function calls 
        @Override
        public SelectBuilder addColumn(final String alias, final String fragment) {

            if (this.colsToFragments.containsKey(alias))
                throw new UncheckedException("Cannot add duplicate alias: " + alias);

            this.colsToFragments.put(
                    alias,
                    " " + fragment + " AS " + notNull(alias));

            return this;

        }

        @Override
        public SelectBuilder addColumn(final String column) {

            return this.addColumn(column, column);

        }

        @Override
        public SelectBuilder addColumnsFrom(final Class<?> klass) {

            return this.addColumnsFrom(null, klass);

        }

        @Override
        public SelectBuilder addColumnsFrom(final String tableAlias, final Class<?> klass) {

            for (final Map.Entry<String,String> entry : PropertyUtil.propertiesToReadWithTypes(
                    klass,
                    tableAlias,
                    PropertyUtil.PropertyAccess.ALL,
                    PropertyUtil.defaultProcessor).entrySet()) {

                this.addColumn(entry.getKey(), entry.getValue());

            }

            return this;

        }

        @Override
        public SelectBuilder removeColumn(final String column) {

            this.colsToFragments.remove(column);

            return this;

        }

        public void configureStatement() {

            if (this.colsToFragments.isEmpty()) throw new UncheckedException("Cannot use an empty select-builder.");

            final String all = listJoiner.join(this.colsToFragments.values());

            statementBuilder.resolveAsText(selectsKey, all);

        }

    }

}
