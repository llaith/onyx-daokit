package org.llaith.onyx.daokit.core.statement.builder;

import org.llaith.onyx.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.toolkit.exception.UncheckedException;
import org.llaith.onyx.toolkit.lang.Guard;
import org.llaith.onyx.toolkit.lang.ParamString;
import org.llaith.onyx.toolkit.lang.StringUtil;

/**
 *
 */
public interface PagingBuilder {

    void addLimit(int limit);

    void addLimitAndOffset(int limit, int offset);

    // If using keyset pagination, use 'limit' only and remember to orderby the key!
    enum PagingSupport {
        H2("LIMIT ${limit}", "LIMIT ${limit} OFFSET ${offset}"),
        MYSQL("LIMIT ${limit}", "LIMIT ${limit} OFFSET ${offset}"),
        MSSQL("FETCH NEXT ${limit} ROWS ONLY", "OFFSET ${offset} ROWS FETCH NEXT ${limit} ROWS ONLY"), // from 2012 only!
        ORACLE("FETCH NEXT ${limit} ROWS ONLY", "OFFSET ${offset} ROWS FETCH NEXT ${limit} ROWS ONLY"), // from 12 only!
        POSTGRES("FETCH NEXT ${limit} ROWS ONLY", "OFFSET ${offset} ROWS FETCH NEXT ${limit} ROWS ONLY"),; // from 9 only!
        private final ParamString keysetPagination;
        private final ParamString offsetPagination;
        PagingSupport(final String keysetPagination, final String offsetPagination) {
            this.keysetPagination = new ParamString(keysetPagination);
            this.offsetPagination = new ParamString(offsetPagination);
        }
    }

    class Impl implements PagingBuilder {

        private final String pagingKey;

        private final PagingSupport support;

        private String fragment;

        private final StatementBuilder statementBuilder;

        public Impl(final StatementBuilder statementBuilder, final String pagingKey, final PagingSupport support) {

            this.statementBuilder = Guard.notNull(statementBuilder);

            this.pagingKey = Guard.notNull(pagingKey);

            this.support = Guard.notNull(support);

        }

        @Override
        public void addLimit(final int limit) {

            if (this.support.keysetPagination == null)
                throw new UnsupportedOperationException("Keyset pagination not supported in database: " + this.support.name());

            this.fragment = this.support.keysetPagination.resolveWith(ParamString.paramsOf(
                    "limit", limit));

        }

        @Override
        public void addLimitAndOffset(final int limit, final int offset) {

            if (this.support.offsetPagination == null)
                throw new UnsupportedOperationException("Offset pagination not supported in database: " + this.support.name());

            this.fragment = this.support.offsetPagination.resolveWith(ParamString.paramsOf(
                    "limit", limit,
                    "offset", offset));

        }

        public void configureStatement() {

            if (StringUtil.isBlankOrNull(this.fragment))
                throw new UncheckedException("Cannot use an empty paging-builder.");

            statementBuilder.resolveAsText(pagingKey, this.fragment);

        }

    }

}
