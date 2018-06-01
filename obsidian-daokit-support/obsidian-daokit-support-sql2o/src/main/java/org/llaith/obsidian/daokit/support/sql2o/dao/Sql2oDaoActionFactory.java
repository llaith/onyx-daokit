package org.llaith.obsidian.daokit.support.sql2o.dao;


import org.llaith.obsidian.daokit.support.sql2o.statement.Sql2oStatementActionFactory;
import org.sql2o.Connection;
import org.llaith.obsidian.daokit.core.dao.EntityDaoActions;

import java.io.Serializable;

/**
 *
 */
public class Sql2oDaoActionFactory {

    public final static String PARAMETER_PREFIX = ":";

    private final Sql2oStatementActionFactory manager = new Sql2oStatementActionFactory();

    public <ID extends Serializable, X> EntityDaoActions<Connection,X,ID> build(final Class<X> klass, final Class<ID> pkClass) {

        return new EntityDaoActions<>(
                this.manager.insertAndReturnIdAs(pkClass),
                this.manager.update(),
                this.manager.selectAs(klass),
                this.manager.queryFor(klass));

    }

}
