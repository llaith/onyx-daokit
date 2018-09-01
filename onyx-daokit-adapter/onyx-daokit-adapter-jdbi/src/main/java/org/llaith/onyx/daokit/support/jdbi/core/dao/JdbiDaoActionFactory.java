package org.llaith.onyx.daokit.support.jdbi.core.dao;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.llaith.onyx.daokit.support.jdbi.core.mapper.JdbiJacksonResultSetMapper;
import org.llaith.onyx.daokit.support.jdbi.core.statement.JdbiStatementActionFactory;
import org.llaith.onyx.daokit.core.dao.EntityDaoActions;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil;

import java.io.Serializable;

/**
 *
 */
public class JdbiDaoActionFactory {

    public final static String PARAMETER_PREFIX = ":";

    private final JdbiStatementActionFactory manager = new JdbiStatementActionFactory();

    public <ID extends Serializable, X> EntityDaoActions<Handle,X,ID> build(final Class<X> klass, final ResultColumnMapper<ID> idMapper) {

        final JdbiJacksonResultSetMapper<X> mapper = new JdbiJacksonResultSetMapper<>(
                klass,
                PropertyUtil.properties(klass, PropertyUtil.PropertyAccess.ALL));

        return build(mapper, idMapper);

    }

    public <ID extends Serializable, X> EntityDaoActions<Handle,X,ID> build(final ResultSetMapper<X> mapper, final ResultColumnMapper<ID> idMapper) {

        return new EntityDaoActions<>(
                this.manager.insertAndReturnIdAs(idMapper),
                this.manager.update(),
                this.manager.selectAs(mapper),
                this.manager.queryFor(mapper));

    }

}
