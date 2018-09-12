package org.llaith.onyx.daokit.core.statement.annotation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.llaith.onyx.toolkit.lang.EnumId;

import java.lang.reflect.Field;

import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;

/**
 *
 */
public interface EnumConverterResources {

    enum TestEnum implements EnumId {

        ONE(123l), TWO(456l);

        private final Long id;

        TestEnum(final Long id) {
            this.id = id;
        }

        @Override
        public Long id() {
            return id;
        }

    }

    class EnumTarget {

        @JsonProperty
        public TestEnum first;
        @JsonProperty
        public TestEnum second;

        @JsonCreator
        public EnumTarget(@JsonProperty("first") final TestEnum first, @JsonProperty("second") final TestEnum second) {
            this.first = first;
            this.second = second;
        }

    }

    class OuterTarget {

        @JsonProperty
        public EnumTarget inner;

        public OuterTarget(final @JsonProperty("inner") EnumTarget inner) {
            this.inner = inner;
        }

    }

    static void testConvertFrom(final Converter converter, final Object target, final String fieldName,
                                final Object fromValue, final Object toValue)
            throws NoSuchFieldException, IllegalAccessException {

        final Field field = target.getClass().getField(fieldName);

        final Object result = converter.fromField(field, field.get(target));

        assertTrue(reflectionEquals(
                result,
                fromValue));

        final Object back = converter.toField(field, result);

        assertTrue(reflectionEquals(
                back,
                toValue));

    }

}
