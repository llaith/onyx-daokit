package org.llaith.onyx.daokit.core.statement.builder;

import com.google.common.base.Joiner;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil;
import org.llaith.onyx.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.toolkit.exception.UncheckedException;
import org.llaith.onyx.toolkit.lang.Guard;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.llaith.onyx.daokit.core.statement.StatementBuilder.INTERNAL_PREFIX;
import static org.llaith.onyx.daokit.core.statement.StatementBuilder.prefix;

/**
 *
 */
public interface UpdateBuilder {

    UpdateBuilder addFragment(String column, String fragment);

    UpdateBuilder addColumn(String column);

    UpdateBuilder addColumn(String column, String param);

    UpdateBuilder addColumnsFrom(Class<?> klass);

    UpdateBuilder removeColumn(String column);

    class Impl implements UpdateBuilder {

        private static final Joiner listJoiner = Joiner.on(", ");

        private final LinkedHashMap<String,String> colsToFragments = new LinkedHashMap<>();

        private final LinkedHashMap<String,String> colsToParams = new LinkedHashMap<>();

        private final String colsKey;

        private final StatementBuilder statementBuilder;

        public Impl(final StatementBuilder statementBuilder, final String colsKey) {

            this.statementBuilder = Guard.notNull(statementBuilder);

            this.colsKey = Guard.notNull(colsKey);

        }

        // used for casts, literals, and function calls 
        @Override
        public UpdateBuilder addFragment(final String column, final String fragment) {

            if (this.colsToFragments.containsKey(column))
                throw new UncheckedException("Cannot add duplicate column: " + column);

            this.colsToFragments.put(
                    column,
                    " " + fragment);

            this.colsToParams.put(column, fragment);

            return this;

        }

        @Override
        public UpdateBuilder addColumn(final String column) {

            return this.addColumn(column, column);

        }

        @Override
        public UpdateBuilder addColumn(final String column, final String param) {

            return this.addFragment(column, column + " = " + prefix(param));

        }

        @Override
        public UpdateBuilder addColumnsFrom(final Class<?> klass) {

            for (final Map.Entry<String,String> entry : PropertyUtil.propertiesToWriteWithTypes(
                    klass,
                    INTERNAL_PREFIX,
                    PropertyUtil.PropertyAccess.EXCLUDE_INVARIANT,
                    PropertyUtil.defaultProcessor).entrySet()) {

                this.addFragment(entry.getKey(), entry.getKey() + " = " + entry.getValue());

            }

            return this;

        }

        @Override
        public UpdateBuilder removeColumn(final String column) {

            final String param = this.colsToFragments.get(Guard.notNull(column));

            if (param != null) {

                this.colsToFragments.remove(column);

                this.colsToParams.remove(column);

            }

            return this;

        }

        public void configureStatement() {

            if (this.colsToFragments.isEmpty()) throw new UncheckedException("Cannot use an empty update-builder.");

            statementBuilder.resolveAsText(
                    colsKey,
                    listJoiner.join(this.colsToFragments.values()));

        }

    }

}
