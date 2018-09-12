package org.llaith.onyx.daokit.support.jdbi.core.mapper;

import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil.PropertyAccess;
import org.llaith.onyx.toolkit.lang.Guard;
import org.llaith.onyx.toolkit.lang.MapUtil;
import org.llaith.onyx.toolkit.reflection.FieldAccessUtil;
import org.llaith.onyx.toolkit.reflection.InstanceUtil;
import org.modelmapper.TypeToken;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class JdbiReflectiveResultSetMapper<T> implements ResultSetMapper<T> {

    private final TypeToken<T> token;

    private final Map<String,String> mappings = new HashMap<>();

    public JdbiReflectiveResultSetMapper(final Class<T> klass) {

        this(TypeToken.of(klass), MapUtil.reverse(PropertyUtil.properties(klass, PropertyAccess.ALL)));

    }

    public JdbiReflectiveResultSetMapper(final TypeToken<T> token) {

        this(token, MapUtil.reverse(PropertyUtil.properties(token.getRawType(), PropertyAccess.ALL)));

    }

    public JdbiReflectiveResultSetMapper(final Class<T> klass, final Map<String,String> mappings) {

        this(TypeToken.of(klass), mappings);

    }

    public JdbiReflectiveResultSetMapper(final TypeToken<T> token, final Map<String,String> mappings) {

        this.token = token;

        this.mappings.putAll(Guard.notNull(mappings));

    }

    @Override
    public T map(int index, ResultSet r, StatementContext ctx) throws SQLException {

        final T instance = InstanceUtil.newInstance(this.token.getRawType());

        final ResultSetMetaData meta = r.getMetaData();

        for (int col = 1; col <= meta.getColumnCount(); col++) {

            final String name = Guard.notBlankOrNull(this.mappings.get(meta.getColumnName(col)));

            final Object value = r.getObject(col);

            FieldAccessUtil.fieldSet(instance, name, value);

        }

        return instance;

    }

}
