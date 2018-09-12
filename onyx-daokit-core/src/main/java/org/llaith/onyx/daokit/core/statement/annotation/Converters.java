package org.llaith.onyx.daokit.core.statement.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.llaith.onyx.toolkit.exception.UncheckedException;
import org.llaith.onyx.toolkit.lang.EnumId;

import java.io.IOException;
import java.lang.reflect.Field;

import static java.lang.String.format;
import static org.llaith.onyx.toolkit.lang.ToStringUtil.asString;

/**
 *
 */
public interface Converters {

    ObjectMapper COMMON_MAPPER = new ObjectMapper().registerModule(new ParameterNamesModule());

    class DefaultConverter implements Converter {

        @Override
        public Object fromField(final Field field, final Object value) {
            return value;
        }

        @Override
        public Object toField(final Field field, final Object value) {
            return value;
        }

        @Override
        public String convertToColumn(final String param) {
            return param;
        }

        @Override
        public String convertFromColumn(final String param) {
            return param;
        }

    }

    class EnumConverter implements Converter {

        @Override
        public Object fromField(final Field field, final Object value) {

            if (value == null) return null;

            return ((Enum)value).name();

        }

        @SuppressWarnings("unchecked")
        @Override
        public Object toField(final Field field, final Object value) {

            if (value == null) return null;

            return Enum.valueOf((Class<Enum>)field.getType(), (String)value);

        }

        @Override
        public String convertToColumn(final String param) {
            return param;
        }

        @Override
        public String convertFromColumn(final String param) {
            return param;
        }

    }

    class EnumIdConverter implements Converter {

        @Override
        public Object fromField(final Field field, final Object value) {

            if (value == null) return null;

            return ((EnumId)value).id();

        }

        @Override
        @SuppressWarnings("unchecked")
        public Object toField(final Field field, final Object value) {

            if (value == null) return null;

            final Class<? extends Enum> ec = (Class<? extends Enum>)field.getType();

            final Long l = (Long)value;

            for (Object e : ec.getEnumConstants()) {
                final EnumId eid = (EnumId)e;
                if (eid.id().equals(l)) return e;
            }

            throw new UncheckedException(format("Cannot find %s with id of: %s", ec, l));

        }

        @Override
        public String convertToColumn(final String param) {
            return param;
        }

        @Override
        public String convertFromColumn(final String param) {
            return param;
        }

    }

    // PG Specific converter
    class JsonConverter implements Converter {

        @Override
        public Object fromField(final Field field, final Object value) {

            if (value == null) return null;

            try {

                return COMMON_MAPPER.writeValueAsString(value);

            } catch (JsonProcessingException e) {

                throw UncheckedException.wrap(format("Failed to serialize-to-json the object: %s", asString(value)), e);

            }

        }

        @Override
        public Object toField(final Field field, final Object value) {

            if (value == null) return null;

            try {

                return COMMON_MAPPER.readValue((String)value, field.getType());

            } catch (IOException e) {

                throw UncheckedException.wrap(format("Failed to serialize-from-json the string: %s", asString(value)), e);

            }

        }

        @Override
        public String convertToColumn(final String param) {
            return " CAST (" + param + " AS jsonb) ";
        }

        @Override
        public String convertFromColumn(final String param) {
            return " CAST (" + param + " AS text) ";
        }

    }

    // PG Specific converter
    class UnconvertedJsonConverter extends DefaultConverter {

        @Override
        public String convertToColumn(final String param) {
            return " CAST (" + param + " AS jsonb) ";
        }

        @Override
        public String convertFromColumn(final String param) {
            return " CAST (" + param + " AS text) ";
        }

    }

}
