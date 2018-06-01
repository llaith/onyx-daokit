package org.llaith.obsidian.daokit.core.statement.annotation;

import java.lang.reflect.Field;

/**
 *
 */
public interface Converter {

    Object fromField(Field field, Object value);

    Object toField(Field field, Object value);

    String convertToColumn(String param);

    String convertFromColumn(String param);

}
