package org.llaith.onyx.daokit.core.dao;

import org.llaith.onyx.daokit.core.statement.ResultCountAction;
import org.llaith.onyx.daokit.core.statement.ResultListAction;
import org.llaith.onyx.daokit.core.statement.ResultObjectAction;
import org.llaith.onyx.toolkit.util.lang.Guard;

import java.io.Serializable;

/**
 * Note, insert can be either update or select depending on use of returning clause (or equivalents)
 */
public class EntityDaoActions<C, T, ID extends Serializable> {

    @FunctionalInterface
    public interface EntityDaoActionsFactory<C, ID extends Serializable> {

        <XT> EntityDaoActions<C,XT,ID> build(Class<XT> daoClass);

    }

    public final ResultObjectAction<C,ID> insertAndReturn;

    public final ResultCountAction<C> update;

    public final ResultObjectAction<C,T> select;

    public final ResultListAction<C,T> query;

    public EntityDaoActions(
            final ResultObjectAction<C,ID> insertAndReturn,
            final ResultCountAction<C> update,
            final ResultObjectAction<C,T> select,
            final ResultListAction<C,T> query) {

        this.insertAndReturn = Guard.notNull(insertAndReturn);
        this.update = Guard.notNull(update);
        this.select = Guard.notNull(select);
        this.query = Guard.notNull(query);

    }

}
