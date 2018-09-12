package org.llaith.onyx.daokit.core.statement;


import org.llaith.onyx.toolkit.pattern.results.ResultCount;

import java.util.function.Function;

/**
 *
 */
public interface ResultCountAction<C> extends Function<ComposedStatement<C>,ResultCount> {

}
