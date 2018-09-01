package org.llaith.onyx.daokit.support.jdbi.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.llaith.onyx.daokit.core.statement.annotation.Column;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil;
import org.llaith.onyx.toolkit.util.exception.ExceptionUtil;
import org.llaith.onyx.toolkit.util.lang.Guard;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class JdbiJacksonResultSetMapper<T> implements ResultSetMapper<T> {

    private final Class<T> klass;

    private final Map<String,String> mappings = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new ParameterNamesModule());

    public JdbiJacksonResultSetMapper(final Class<T> klass, final Map<String,String> mappings) {

        this.klass = Guard.notNull(klass);

        this.mappings.putAll(Guard.notNull(mappings));

    }

    @Override
    public T map(int index, ResultSet r, StatementContext ctx) throws SQLException {

        final Map<String,Field> properties = PropertyUtil.propertyFields(
                this.klass,
                PropertyUtil.PropertyAccess.ALL,
                PropertyUtil.defaultProcessor);

        return mapper.convertValue(buildValueMap(properties, r), this.klass);

    }

    private Map<String,Object> buildValueMap(final Map<String,Field> properties, final ResultSet r) throws SQLException {

        final Map<String,Object> values = new HashMap<>();

        final ResultSetMetaData meta = r.getMetaData();

        for (int col = 1; col <= meta.getColumnCount(); col++) {

            final String colName = meta.getColumnName(col);

            final String name = this.mappings.containsKey(colName) ?
                    Guard.notBlankOrNull(this.mappings.get(colName)) :
                    colName;

            final Object o = r.getObject(col);

            final Field f = properties.get(colName);

            final Object value = ExceptionUtil.rethrowOrReturn(() -> Guard.notNull(f.getAnnotation(Column.class).converter())
                                                                          .newInstance()
                                                                          .toField(f, o));

            values.put(name, value);

        }

        return values;

    }

}
