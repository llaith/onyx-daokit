package org.llaith.obsidian.daokit.core.dao.simple;

import com.codahale.metrics.MetricRegistry;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;
import org.llaith.obsidian.daokit.core.statement.ComposedStatementBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.WhereBuilder;
import org.llaith.onyx.toolkit.results.ResultCount;
import org.llaith.onyx.toolkit.results.ResultList;
import org.llaith.onyx.toolkit.results.ResultObject;
import org.llaith.onyx.toolkit.util.lang.Guard;
import org.llaith.onyx.toolkit.util.reflection.MethodAccessUtil;
import org.llaith.obsidian.daokit.core.dao.BaseDao;
import org.llaith.obsidian.daokit.core.dao.EntityDaoActions;

import java.util.Date;
import java.util.function.Consumer;

/**
 * Note, all 'externalId-based', (except resolveIdStatement) methods respect
 * the soft-deletes, and all the 'id-based' methods ignore it.
 */
public class SimpleDao<C, T extends SimpleEntity> extends BaseDao<C,Long,T> {

    public SimpleDao(
            final OrmBuilder orm,
            final EntityDaoActions<C,T,Long> actions,
            final MetricRegistry metrics,
            final C connection) {

        super(orm, actions, metrics, connection);

    }

    @Override
    public ResultObject<T> createConditionally(T entity, String checkTable, Consumer<WhereBuilder> whereBuilder) {

        metadataAccess(Guard.notNull(entity)).init(new Date());

        return super.createConditionally(entity, checkTable, whereBuilder);

    }

    @Override
    public ResultObject<T> read(Long aLong) {
        return super.read(aLong);
    }

    @Override
    public ResultObject<T> create(final T entity) {

        metadataAccess(Guard.notNull(entity)).init(new Date());

        return super.create(entity);

    }

    @Override
    public ResultCount update(final T entity) {

        metadataAccess(Guard.notNull(entity)).update(new Date());

        return super.update(entity);

    }

    @Override
    public ResultCount delete(Long aLong) {
        return super.delete(aLong);
    }

    @Override
    public ResultList<T> listAll(int limit) {
        return super.listAll(limit);
    }

    public ResultCount updateWithCheck(final T entity) {

        final SimpleEntity.Metadata metadata = metadataAccess(Guard.notNull(entity)).update(new Date());

        return ComposedStatementBuilder
                .from(this.orm.createUpdateWithCheckSql())
                .setParametersFrom(entity)
                .setParameter("id", entity.getId()) // we need to add the 'read-only' param back because its for a where
                .setParameter("optimistic_lock", metadata.versionCheck())
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update);

    }

    public ResultCount deleteWithCheck(final Integer id, final Integer updateCount) {

        return ComposedStatementBuilder
                .from(this.orm.createDeleteWithCheckSql())
                .setParameter("id", id)
                .setParameter("optimistic_lock", updateCount)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update);

    }

    public ResultObject<T> readByExternalId(final String externalId) {

        return ComposedStatementBuilder
                .from(this.orm.createLoadByExternalIdSql())
                .setParameter("external_id", externalId)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeSelect(this.actions.select);

    }

    public ResultObject<T> readByExternalIdWithCheck(final String externalId, final Integer updateCount) {

        return ComposedStatementBuilder
                .from(this.orm.createLoadByExternalIdWithCheckSql())
                .setParameter("external_id", externalId)
                .setParameter("optimistic_lock", updateCount)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeSelect(this.actions.select);

    }

    public ResultCount updateByExternalIdWithCheck(final T entity) {

        final SimpleEntity.Metadata metadata = metadataAccess(Guard.notNull(entity)).update(new Date());

        return ComposedStatementBuilder
                .from(this.orm.createUpdateByExternalIdWithCheckSql())
                .setParametersFrom(entity)
                .setParameter("optimistic_lock", metadata.versionCheck())
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update);

    }

    public ResultCount deleteByExternalIdWithCheck(final String externalId, final Integer updateCount) {

        return ComposedStatementBuilder
                .from(this.orm.createDeleteByExternalIdWithCheckSql())
                .setParameter("external_id", externalId)
                .setParameter("optimistic_lock", updateCount)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update);

    }

    public ResultCount softDeleteWithCheck(final T entity) {

        metadataAccess(entity).delete(new Date());

        return this.updateWithCheck(entity);

    }

    private SimpleEntity.Metadata metadataAccess(final T entity) {

        return MethodAccessUtil.callGetter(entity, SimpleEntity.Metadata.class, "metadata");

    }

}
