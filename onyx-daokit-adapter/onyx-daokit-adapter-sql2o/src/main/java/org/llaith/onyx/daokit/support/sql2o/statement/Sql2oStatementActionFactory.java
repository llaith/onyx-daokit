package org.llaith.onyx.daokit.support.sql2o.statement;

import org.llaith.onyx.toolkit.results.ResultCount;
import org.llaith.onyx.toolkit.results.ResultList;
import org.llaith.onyx.toolkit.results.ResultObject;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.llaith.onyx.daokit.core.statement.ComposedStatement;
import org.llaith.onyx.daokit.core.statement.ComposedStatementException;
import org.llaith.onyx.daokit.core.statement.ResultCountAction;
import org.llaith.onyx.daokit.core.statement.ResultListAction;
import org.llaith.onyx.daokit.core.statement.ResultObjectAction;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil.PropertyAccess;
import org.llaith.onyx.toolkit.util.exception.ThrowableFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Sql2oStatementActionFactory {

    public <X> ResultObjectAction<Connection,X> insertAndReturnIdAs(final Class<X> klass) {

        return composed -> new ResultObject<>(
                this.buildStatement(composed, true)
                    .executeUpdate()
                    .getKey(klass),
                this.exceptionFactory(composed));

    }

    public <X> ResultListAction<Connection,X> queryFor(final Class<X> klass) {

        return composed -> new ResultList<>(
                this.buildStatement(composed, false, klass)
                    .executeAndFetch(klass),
                this.exceptionFactory(composed));

    }

    public ResultListAction<Connection,Map<String,Object>> query() {

        return composed -> new ResultList<>(
                this.buildStatement(composed, false)
                    .executeAndFetchTable()
                    .asList(),
                this.exceptionFactory(composed));

    }

    public <X> ResultObjectAction<Connection,X> selectAs(final Class<X> klass) {

        return composed -> new ResultObject<>(
                this.first(this.buildStatement(composed, false, klass)
                               .executeAndFetch(klass)),
                this.exceptionFactory(composed));

    }

    public ResultObjectAction<Connection,Map<String,Object>> select() {

        return composed -> new ResultObject<>(
                this.first(this.buildStatement(composed, false)
                               .executeAndFetchTable().asList()),
                this.exceptionFactory(composed));

    }

    public ResultCountAction<Connection> update() {

        return composed -> new ResultCount(
                this.buildStatement(composed, false)
                    .executeUpdate()
                    .getResult(),
                this.exceptionFactory(composed));

    }

    @Nullable
    private <X> X first(@Nullable final List<X> results) {

        if (results == null) return null;

        if (results.isEmpty()) return null;

        return results.get(0);

    }

    private Query buildStatement(final ComposedStatement<Connection> composed, final boolean returnKeys, final Class<?> klass) {

        final Query query = this.buildStatement(composed, returnKeys);

        query.setColumnMappings(PropertyUtil.properties(klass, PropertyAccess.ALL));

        return query;

    }

    private Query buildStatement(final ComposedStatement<Connection> composed, final boolean returnKeys) {

        final Query query = composed.connection().createQuery(composed.statement(), returnKeys);

        for (final String name : composed.params()) {

            query.addParameter(name, composed.args().get(name));

        }

        return query;

    }

    private ThrowableFactory.ExceptionWithoutCause<RuntimeException> exceptionFactory(final ComposedStatement<Connection> composed) {

        return msg -> new ComposedStatementException(composed, msg);

    }


}
