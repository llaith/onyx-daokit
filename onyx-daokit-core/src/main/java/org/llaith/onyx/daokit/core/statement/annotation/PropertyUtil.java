package org.llaith.onyx.daokit.core.statement.annotation;

import org.llaith.onyx.toolkit.lang.Guard;
import org.llaith.onyx.toolkit.reflection.ClassStructureUtil;
import org.llaith.onyx.toolkit.reflection.FieldAccessUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.llaith.onyx.toolkit.fn.ExcecutionUtil.rethrowOrReturn;
import static org.llaith.onyx.toolkit.lang.StringUtil.camelCaseToSnakeCase;
import static org.llaith.onyx.toolkit.lang.StringUtil.notBlankOrNull;
import static org.llaith.onyx.toolkit.reflection.InstanceUtil.newInstance;

/**
 *
 */
public final class PropertyUtil {

    public enum PropertyAccess {
        ALL, EXCLUDE_INVARIANT, EXCLUDE_INVARIANT_PLUS_AUTO
    }

    public interface PropertyAnnotationProcessor {

        String name(Field field, PropertyAccess propertyAccess);

    }

    public static PropertyAnnotationProcessor defaultProcessor = (field, propertyAccess) -> {

        final Column column = field.getAnnotation(Column.class);

        if (column == null) return null; // not mapped
        if ((propertyAccess == PropertyAccess.EXCLUDE_INVARIANT) && column.invariable()) return null;
        if ((propertyAccess == PropertyAccess.EXCLUDE_INVARIANT_PLUS_AUTO) && column.invariable() && column.auto())
            return null;

        return Guard.defaultIfNullOrBlank(
                field.getAnnotation(Column.class).value(),
                camelCaseToSnakeCase(field.getName()));

    };

    public static class DefaultReader implements BiFunction<Field,Object,Object> {

        @Override
        public Object apply(final Field field, final Object o) {

            if (field.isAnnotationPresent(Column.class))
                return rethrowOrReturn(() -> Guard.notNull(field.getAnnotation(Column.class).converter()).newInstance().fromField(field, o));

            return o;

        }

    }

    private PropertyUtil() {}

    public static Map<String,String> properties(final Class<?> klass, final PropertyAccess propertyAccess) {

        return properties(klass, propertyAccess, defaultProcessor);

    }

    public static Map<String,String> properties(final Class<?> klass, final PropertyAccess propertyAccess, final PropertyAnnotationProcessor processor) {

        final Map<String,String> map = new HashMap<>();

        for (final Map.Entry<String,Field> entry : propertyFields(klass, propertyAccess, processor).entrySet()) {

            map.put(entry.getKey(), entry.getValue().getName());

        }

        return map;

    }

    public static Map<String,Field> propertyFields(final Class<?> klass, final PropertyAccess propertyAccess, final PropertyAnnotationProcessor processor) {

        final Map<String,Field> map = new HashMap<>();

        for (final Field field : ClassStructureUtil.declaredFieldsOf(klass)) {

            final String name = Guard.notNull(processor).name(field, propertyAccess);

            if (notBlankOrNull(name)) map.put(
                    name,
                    field);

        }

        return map;

    }

    public static Map<String,String> propertiesToReadWithTypes(
            final Class<?> klass,
            final String tableAlias,
            final PropertyAccess propertyAccess,
            final PropertyAnnotationProcessor processor) {

        final Map<String,String> map = new HashMap<>();

        for (final Map.Entry<String,Field> entry : propertyFields(klass, propertyAccess, processor).entrySet()) {

            final String name = entry.getKey();

            final Field field = entry.getValue();

            final String qualifiedName = tableAlias != null ?
                    tableAlias + "." + name : name;

            final String convertedField =
                    field.isAnnotationPresent(Column.class) ?
                            newInstance(field.getAnnotation(Column.class).converter()).convertFromColumn(qualifiedName) :
                            qualifiedName;

            map.put(name, convertedField);

        }

        return map;

    }

    public static Map<String,String> propertiesToWriteWithTypes(
            final Class<?> klass,
            final String prefix,
            final PropertyAccess propertyAccess,
            final PropertyAnnotationProcessor processor) {

        Guard.notNull(prefix);

        final Map<String,String> map = new HashMap<>();

        for (final Map.Entry<String,Field> entry : propertyFields(klass, propertyAccess, processor).entrySet()) {

            final String name = entry.getKey();

            final Field field = entry.getValue();

            final String convertedField =
                    field.isAnnotationPresent(Column.class) ?
                            newInstance(field.getAnnotation(Column.class).converter()).convertToColumn(prefix + name) :
                            prefix + name;

            map.put(name, convertedField);

        }

        return map;

    }

    public static Map<String,Object> pullPropertyValuesFrom(final Object o, final Map<String,Object> props) {

        for (Map.Entry<String,String> entry : properties(
                Guard.notNull(o).getClass(),
                PropertyAccess.ALL).entrySet()) {

            props.put(entry.getKey(), FieldAccessUtil.fieldGet(o, entry.getValue()));

        }

        return props;

    }

    public static Map<String,Serializable> pullSerializablePropertyValuesFrom(
            final Object o,
            final BiFunction<Field,Object,Object> reader,
            final Map<String,Serializable> props) {

        for (Map.Entry<String,String> entry : properties(
                Guard.notNull(o).getClass(),
                PropertyAccess.ALL).entrySet()) {

            props.put(entry.getKey(), (Serializable)FieldAccessUtil.fieldGet(o, entry.getValue(), reader));

        }

        return props;

    }

    public static <X> X pushPropertyValuesTo(final X o) {

        for (Map.Entry<String,String> entry : properties(
                Guard.notNull(o).getClass(),
                PropertyAccess.EXCLUDE_INVARIANT_PLUS_AUTO).entrySet()) {

            FieldAccessUtil.fieldSet(o, entry.getKey(), entry.getValue());

        }

        return o;

    }

}
