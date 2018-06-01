package org.llaith.obsidian.daokit.core.statement;


import org.llaith.onyx.toolkit.results.ResultCount;

import java.util.function.Function;

/**
 *
 */
public interface ResultCountAction<C> extends Function<ComposedStatement<C>,ResultCount> {

}
