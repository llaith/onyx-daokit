package org.llaith.obsidian.daokit.core.statement;


import org.llaith.onyx.toolkit.results.ResultList;

import java.util.function.Function;

/**
 *
 */
public interface ResultListAction<C, R> extends Function<ComposedStatement<C>,ResultList<R>> {

}
