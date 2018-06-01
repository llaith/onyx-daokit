package org.llaith.obsidian.daokit.core.dao.insertonly;

import com.codahale.metrics.MetricRegistry;
import org.llaith.onyx.toolkit.results.ResultList;
import org.llaith.onyx.toolkit.results.ResultObject;
import org.llaith.obsidian.daokit.core.dao.BaseDao;
import org.llaith.obsidian.daokit.core.dao.EntityDaoActions;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;

import java.util.UUID;

/**
 * Note, all 'externalId-based', (except resolveIdStatement) methods respect
 * the soft-deletes, and all the 'id-based' methods ignore it.
 */
public class ImmutableEntityDao<C, T extends ImmutableEntity> extends BaseDao<C,UUID,T> {

    public ImmutableEntityDao(
            final OrmBuilder ormBuilder,
            final EntityDaoActions<C,T,UUID> actions,
            final MetricRegistry metrics,
            final C connection) {

        super(ormBuilder, actions, metrics, connection);

    }

    @Override
    public ResultObject<T> create(final T entity) {

        return super.create(entity);

    }

    @Override
    public ResultObject<T> read(final UUID id) {

        return super.read(id);

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
