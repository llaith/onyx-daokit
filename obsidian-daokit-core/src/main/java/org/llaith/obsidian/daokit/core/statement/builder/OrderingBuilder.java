package org.llaith.obsidian.daokit.core.statement.builder;

import com.google.common.base.Joiner;
import org.llaith.obsidian.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;
import org.llaith.onyx.toolkit.util.lang.Guard;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public interface OrderingBuilder {


    // surprised, it's PER COLUMN! https://www.postgresql.org/docs/8.3/static/queries-order.html
    void addColumn(String column, boolean asc);

    void addColumn(String column, boolean asc, boolean nullsLast);

    class Impl implements OrderingBuilder {

        private static final Joiner listJoiner = Joiner.on(", ");

        private final List<String> fragment = new ArrayList<>();

        private final String key;

        private final StatementBuilder statementBuilder;

        public Impl(final StatementBuilder statementBuilder, final String key) {

            this.statementBuilder = Guard.notNull(statementBuilder);

            this.key = Guard.notNull(key);

        }

        @Override
        public void addColumn(final String column, final boolean asc) {

            fragment.add(
                    Guard.notNull(column)
                            + (asc ? " ASC" : " DESC"));

        }

        @Override
        public void addColumn(final String column, final boolean asc, final boolean nullsLast) {

            fragment.add(Guard.notNull(column)
                                 + (asc ? " ASC" : " DESC") + " NULLS "
                                 + (nullsLast ? " LAST" : " FIRST"));

        }

        public void configureStatement() {

            if (this.fragment.isEmpty()) throw new UncheckedException("Cannot use an empty ordering-builder.");

            this.statementBuilder.resolveAsText(key, " ORDER BY " + listJoiner.join(this.fragment));

        }

    }

}
