package org.llaith.obsidian.daokit.core.statement.annotation;

import org.junit.Test;

/**
 *
 */
public class EnumConverterTest {

    final EnumConverterResources.EnumTarget enumTarget = new EnumConverterResources.EnumTarget(EnumConverterResources.TestEnum.ONE, EnumConverterResources.TestEnum.TWO);

    final EnumConverterResources.OuterTarget outerTarget = new EnumConverterResources.OuterTarget(enumTarget);

    @Test
    public void testDefaultConverter() throws NoSuchFieldException, IllegalAccessException {

        EnumConverterResources.testConvertFrom(
                new Converters.DefaultConverter(),
                enumTarget,
                "first",
                EnumConverterResources.TestEnum.ONE,
                EnumConverterResources.TestEnum.ONE);

        EnumConverterResources.testConvertFrom(
                new Converters.DefaultConverter(),
                enumTarget,
                "second",
                EnumConverterResources.TestEnum.TWO,
                EnumConverterResources.TestEnum.TWO);


    }

    @Test
    public void testEnumConverter() throws NoSuchFieldException, IllegalAccessException {

        EnumConverterResources.testConvertFrom(
                new Converters.EnumConverter(),
                enumTarget,
                "first",
                "ONE",
                EnumConverterResources.TestEnum.ONE);

        EnumConverterResources.testConvertFrom(
                new Converters.EnumConverter(),
                enumTarget,
                "second",
                "TWO",
                EnumConverterResources.TestEnum.TWO);


    }

    @Test
    public void testEnumIdConverter() throws NoSuchFieldException, IllegalAccessException {

        EnumConverterResources.testConvertFrom(
                new Converters.EnumIdConverter(),
                enumTarget,
                "first",
                123l,
                EnumConverterResources.TestEnum.ONE);

        EnumConverterResources.testConvertFrom(
                new Converters.EnumIdConverter(),
                enumTarget,
                "second",
                456l,
                EnumConverterResources.TestEnum.TWO);


    }

    @Test
    public void testJsonConverter() throws NoSuchFieldException, IllegalAccessException {

        EnumConverterResources.testConvertFrom(
                new Converters.JsonConverter(),
                outerTarget,
                "inner",
                "{\"first\":\"ONE\",\"second\":\"TWO\"}",
                outerTarget.inner);

    }

}
