package org.llaith.onyx.daokit.support.jdbi.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.llaith.onyx.toolkit.lang.Guard;

import static org.llaith.onyx.toolkit.fn.ExcecutionUtil.rethrowOrReturn;


/**
 *
 */
public class JdbiJacksonObjectMapper {

    private final ObjectMapper mapper;

    public JdbiJacksonObjectMapper() {

        this(new ObjectMapper().registerModule(new ParameterNamesModule()));

    }

    public JdbiJacksonObjectMapper(final ObjectMapper mapper) {

        this.mapper = Guard.notNull(mapper);

    }

    public <X> ResultSetMapper<X> mapperFor(final Class<X> klass) {

        return mapperFor(klass, 1);

    }

    public <X> ResultSetMapper<X> mapperFor(final Class<X> klass, final int colIndex) {

        return (index, r, ctx) -> rethrowOrReturn(() -> mapper.readValue(
                r.getString(colIndex),
                Guard.notNull(klass)));

    }

    public <X> ResultSetMapper<X> mapperFor(final Class<X> klass, final String col) {

        return (index, r, ctx) -> rethrowOrReturn(() -> mapper.readValue(
                r.getString(col),
                Guard.notNull(klass)));

    }

}
