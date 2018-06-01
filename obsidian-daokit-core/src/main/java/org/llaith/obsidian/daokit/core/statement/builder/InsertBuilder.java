package org.llaith.obsidian.daokit.core.statement.builder;

import com.google.common.base.Joiner;
import org.llaith.obsidian.daokit.core.statement.annotation.PropertyUtil;
import org.llaith.obsidian.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.llaith.obsidian.daokit.core.statement.StatementBuilder.INTERNAL_PREFIX;
import static org.llaith.obsidian.daokit.core.statement.StatementBuilder.prefix;
import static org.llaith.onyx.toolkit.util.lang.Guard.notNull;

/**
 * Interface is not to allow it to be swapped out (it woudlnt <work that way, but to give a restricted subset of
 * features to the consumer of the builder.
 */
public interface InsertBuilder {

    // used for casts, literals, and function calls 
    InsertBuilder addFragment(String column, String fragment);

    InsertBuilder addColumn(String column);

    InsertBuilder addColumn(String column, String param);

    InsertBuilder addColumnsFrom(Class<?> klass);

    InsertBuilder addColumnsFrom(Class<?> klass, PropertyUtil.PropertyAccess propertyAccess);

    InsertBuilder removeColumn(String column);

    class Impl implements InsertBuilder {

        private static final Joiner listJoiner = Joiner.on(", ");

        private final LinkedHashMap<String,String> colsToVars = new LinkedHashMap<>();

        private final StatementBuilder statementBuilder;

        private final String colsKey;

        private final String varsKey;

        public Impl(final StatementBuilder statementBuilder, final String colsKey, final String varsKey) {

            this.statementBuilder = notNull(statementBuilder);

            this.colsKey = notNull(colsKey);

            this.varsKey = notNull(varsKey);

        }

        // used for casts, literals, and function calls 
        @Override
        public InsertBuilder addFragment(final String column, final String fragment) {

            if (this.colsToVars.containsKey(column))
                throw new UncheckedException("Cannot add duplicate column: " + column);

            this.colsToVars.put(
                    notNull(column),
                    fragment);

            return this;

        }

        @Override
        public InsertBuilder addColumn(final String column) {

            return this.addColumn(column, column);

        }

        @Override
        public InsertBuilder addColumn(final String column, final String param) {

            return this.addFragment(column, prefix(param));

        }

        @Override
        public InsertBuilder addColumnsFrom(final Class<?> klass) {

            // default is to exclude only auto, the invariant is used for insert-only dbs
            return addColumnsFrom(klass, PropertyUtil.PropertyAccess.EXCLUDE_INVARIANT_PLUS_AUTO);

        }

        @Override
        public InsertBuilder addColumnsFrom(final Class<?> klass, final PropertyUtil.PropertyAccess propertyAccess) {

            for (final Map.Entry<String,String> entry : PropertyUtil.propertiesToWriteWithTypes(
                    klass,
                    INTERNAL_PREFIX,
                    propertyAccess,
                    PropertyUtil.defaultProcessor).entrySet()) {

                this.addFragment(entry.getKey(), entry.getValue());

            }

            return this;

        }

        @Override
        public InsertBuilder removeColumn(final String column) {

            final String var = this.colsToVars.get(notNull(column));

            if (var != null) {

                this.colsToVars.remove(column);

            }

            return this;

        }

        public void configureStatement() {

            if (this.colsToVars.isEmpty()) throw new UncheckedException("Cannot use an empty insert-builder.");

            // iteration is safely ordered between keySet and values()
            // http://stackoverflow.com/questions/2923856/is-the-order-guaranteed-for-the-return-of-keys-and-values-from-a-linkedhashmap-o
            this.statementBuilder
                    .resolveAsText(colsKey, listJoiner.join(this.colsToVars.keySet()))
                    .resolveAsText(varsKey, listJoiner.join(this.colsToVars.values()));

        }

    }

}
