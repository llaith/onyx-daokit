package org.llaith.onyx.daokit.core.statement;


import org.llaith.onyx.toolkit.pattern.results.ResultList;

import java.util.function.Function;

/**
 *
 */
public interface ResultListAction<C, R> extends Function<ComposedStatement<C>,ResultList<R>> {

}
