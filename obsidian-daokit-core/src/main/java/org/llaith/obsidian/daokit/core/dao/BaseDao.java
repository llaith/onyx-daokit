package org.llaith.obsidian.daokit.core.dao;

import com.codahale.metrics.MetricRegistry;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;
import org.llaith.obsidian.daokit.core.statement.ComposedStatementBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.WhereBuilder;
import org.llaith.onyx.toolkit.results.ResultCount;
import org.llaith.onyx.toolkit.results.ResultObject;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Note, all 'externalId-based', (except resolveIdStatement) methods respect
 * the soft-deletes, and all the 'id-based' methods ignore it.
 */
public abstract class BaseDao<C, ID extends Serializable, T extends BaseEntity<ID>> extends BaseReadOnlyDao<C,ID,T> {

    public BaseDao(
            final OrmBuilder ormBuilder,
            final EntityDaoActions<C,T,ID> actions,
            final MetricRegistry metrics,
            final C connection) {

        super(ormBuilder, actions, metrics, connection);

    }

    protected ResultObject<T> create(final T entity) {

        return ComposedStatementBuilder
                .from(this.orm.createInsertSql())
                .setParametersFrom(entity)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeSelect(this.actions.select);
        // using this approach works with non-postgres dbs (no insert-returning) but puts id into passed in object instead of a 
        // new object which this approach uses! 
//                .mapResult(id -> {
//                    // if sql2o and jdbi supported insert-returning, we wouldn't have to do this hacky stuff!
//                    // anyway, if we cant get the keys, we return object anyway, user can reload manually.
//                    FieldAccessUtil.fieldSet(entity, "id", id);
//                    return entity;
//                });

    }

    protected ResultObject<T> createConditionally(final T entity, final String checkTable, final Consumer<WhereBuilder> whereBuilder) {

        return ComposedStatementBuilder
                .from(this.orm.createInsertConditionallySql(checkTable, whereBuilder))
                .setParametersFrom(entity)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeSelect(this.actions.select);
//                .mapResult(id -> {
//                    // if we cant get the keys, we return object anyway, user can reload manually.
//                    FieldAccessUtil.fieldSet(entity, "id", id);
//                    return entity;
//                });

    }

    protected ResultCount update(final T entity) {

        return ComposedStatementBuilder
                .from(this.orm.createUpdateSql())
                .setParametersFrom(entity)
                .setParameter("id", entity.getId()) // we need to add the 'read-only' param back because its for a where
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update);

    }

    protected ResultCount delete(final ID id) {

        return ComposedStatementBuilder
                .from(this.orm.createDeleteSql())
                .setParameter("id", id)
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update);

    }

}
