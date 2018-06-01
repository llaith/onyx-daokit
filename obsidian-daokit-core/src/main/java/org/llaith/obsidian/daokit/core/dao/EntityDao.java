package org.llaith.obsidian.daokit.core.dao;

import com.codahale.metrics.MetricRegistry;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.WhereBuilder;
import org.llaith.onyx.toolkit.results.ResultCount;
import org.llaith.onyx.toolkit.results.ResultList;
import org.llaith.onyx.toolkit.results.ResultObject;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Note, all 'externalId-based', (except resolveIdStatement) methods respect
 * the soft-deletes, and all the 'id-based' methods ignore it.
 */
public final class EntityDao<C, ID extends Serializable, T extends BaseEntity<ID>> extends BaseDao<C,ID,T> {


    public EntityDao(
            final OrmBuilder<T> orm,
            final EntityDaoActions<C,T,ID> actions,
            final MetricRegistry metrics,
            final C connection) {

        super(orm, actions, metrics, connection);

    }

    @Override
    public ResultObject<T> create(final T entity) {
        return super.create(entity);
    }

    @Override
    public ResultObject<T> createConditionally(final T entity, final String checkTable, final Consumer<WhereBuilder> whereBuilder) {
        return super.createConditionally(entity, checkTable, whereBuilder);
    }

    @Override
    public ResultObject<T> read(final ID id) {
        return super.read(id);
    }

    @Override
    public ResultCount update(final T entity) {
        return super.update(entity);
    }

    @Override
    public ResultCount delete(final ID id) {
        return super.delete(id);
    }

    @Override
    public ResultList<T> listAll(final int limit) {
        return super.listAll(limit);
    }

    @Override
    public ResultList<T> listAllWithOffset(final int limit, final int offset) {
        return super.listAllWithOffset(limit, offset);
    }

}
