package org.llaith.obsidian.daokit.core.statement;


import org.llaith.onyx.toolkit.results.ResultObject;

import java.util.function.Function;

/**
 *
 */
public interface ResultObjectAction<C, R> extends Function<ComposedStatement<C>,ResultObject<R>> {

}
