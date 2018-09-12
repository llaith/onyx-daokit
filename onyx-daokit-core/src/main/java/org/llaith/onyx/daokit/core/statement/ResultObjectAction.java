package org.llaith.onyx.daokit.core.statement;


import org.llaith.onyx.toolkit.pattern.results.ResultObject;

import java.util.function.Function;

/**
 *
 */
public interface ResultObjectAction<C, R> extends Function<ComposedStatement<C>,ResultObject<R>> {

}
