package org.llaith.onyx.daokit.core.dao;

import com.codahale.metrics.MetricRegistry;
import org.llaith.onyx.daokit.core.orm.OrmBuilder;
import org.llaith.onyx.daokit.core.statement.ComposedStatementBuilder;
import org.llaith.onyx.toolkit.results.ResultList;
import org.llaith.onyx.toolkit.results.ResultObject;

import java.io.Serializable;

import static org.llaith.onyx.toolkit.util.lang.Guard.notNull;

/**
 * Note, all 'externalId-based', (except resolveIdStatement) methods respect
 * the soft-deletes, and all the 'id-based' methods ignore it.
 */
public abstract class BaseReadOnlyDao<C, ID extends Serializable, T extends BaseEntity<ID>> {

    protected final OrmBuilder orm;

    protected final EntityDaoActions<C,T,ID> actions;
    protected final MetricRegistry metrics;

    protected final C connection;

    public BaseReadOnlyDao(
            final OrmBuilder orm,
            final EntityDaoActions<C,T,ID> actions,
            final MetricRegistry metrics,
            final C connection) {

        this.orm = notNull(orm);

        this.actions = notNull(actions);
        this.metrics = notNull(metrics);

        this.connection = notNull(connection);

    }

    protected ResultObject<T> read(final ID id) {

        return ComposedStatementBuilder
                .from(this.orm.createLoadSql())
                .setParameter("id", id)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeSelect(this.actions.select);

    }

    protected ResultList<T> listAll(final int limit) {

        return ComposedStatementBuilder
                .from(this.orm.createListAllSql(limit))
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeQuery(this.actions.query);

    }

    protected ResultList<T> listAllWithOffset(final int limit, final int offset) {

        return ComposedStatementBuilder
                .from(this.orm.createListAllWithOffsetSql(limit, offset))
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeQuery(this.actions.query);

    }

}
