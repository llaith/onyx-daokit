package org.llaith.obsidian.daokit.core.dao.insertonly;

import com.codahale.metrics.MetricRegistry;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;
import org.llaith.obsidian.daokit.core.statement.ComposedStatementBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.WhereBuilder;
import org.llaith.onyx.toolkit.results.ResultList;
import org.llaith.onyx.toolkit.results.ResultObject;
import org.llaith.obsidian.daokit.core.dao.BaseDao;
import org.llaith.obsidian.daokit.core.dao.EntityDaoActions;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Note, all 'externalId-based', (except resolveIdStatement) methods respect
 * the soft-deletes, and all the 'id-based' methods ignore it.
 */
public class VersionedEntityDao<C, T extends VersionedEntity> extends BaseDao<C,Long,T> {

    public VersionedEntityDao(
            final OrmBuilder orm,
            final EntityDaoActions<C,T,Long> actions,
            final MetricRegistry metrics, final C connection) {

        super(orm, actions, metrics, connection);

    }

    @Override
    public ResultObject<T> create(final T entity) {

        return super.create(entity);

    }

    @Override
    public ResultObject<T> createConditionally(final T entity, final String checkTable, Consumer<WhereBuilder> whereBuilder) {

        return super.createConditionally(entity, checkTable, whereBuilder);

    }

    @Override
    public ResultObject<T> read(final Long id) {

        return super.read(id);

    }

    public ResultObject<T> createRevision(final T entity) {

        return ComposedStatementBuilder
                .from(this.orm.createInsertVersionedSql())
                .setParametersFrom(entity)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeSelect(this.actions.select);

    }


    public ResultObject<T> readByConsistentId(final UUID consistentId) {

        // technically doesn't need to be a VersionedEntity, but must have the @ConsistentId field
        final String consistentIdName = this.orm.expectConsistentIdName();

        return ComposedStatementBuilder
                .from(this.orm.createLoadVersionedSql())
                .setParameter(consistentIdName, consistentId)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeSelect(this.actions.select);

    }

    public ResultList<T> listAll(final String tablealias, final int limit) {

        return ComposedStatementBuilder
                .from(this.orm.createListAllVersionedSql(tablealias, limit))
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeQuery(this.actions.query);

    }

    public ResultList<T> listAllWithOffset(final String tablealias, final int limit, final int offset) {

        return ComposedStatementBuilder
                .from(this.orm.createListAllVersionedWithOffsetSql(tablealias, limit, offset))
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeQuery(this.actions.query);

    }

    public ResultList<T> listHistory(final UUID consistentId, final int limit) {

        // technically doesn't need to be a VersionedEntity, but must have the @ConsistentId field
        final String consistentIdName = this.orm.expectConsistentIdName();

        return ComposedStatementBuilder
                .from(this.orm.createListVersionedHistorySql(limit))
                .setParameter(consistentIdName, consistentId)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeQuery(this.actions.query);

    }

}
