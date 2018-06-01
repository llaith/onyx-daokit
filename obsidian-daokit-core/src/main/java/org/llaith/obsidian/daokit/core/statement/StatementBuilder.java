/*
 * Copyright (c) 2016.
 */

package org.llaith.obsidian.daokit.core.statement;

import org.llaith.obsidian.daokit.core.statement.builder.InsertBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.OrderingBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.PagingBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.SelectBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.UpdateBuilder;
import org.llaith.obsidian.daokit.core.statement.builder.WhereBuilder;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;
import org.llaith.onyx.toolkit.util.lang.Guard;
import org.llaith.onyx.toolkit.util.lang.ParamString;
import org.llaith.onyx.toolkit.util.lang.StringUtil;
import org.llaith.onyx.toolkit.util.lang.UuidUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.llaith.onyx.toolkit.util.io.FileUtil.readFromClasspath;
import static org.llaith.onyx.toolkit.util.io.FileUtil.readFromPath;
import static org.llaith.onyx.toolkit.util.lang.Guard.notBlankOrNull;

/**
 * Because these statements can be extended, the code to rewrite the prefixes cannot be in the statement class.
 */
public final class StatementBuilder {

    public static final String INTERNAL_PREFIX = "@:";

    private static final String PARAMETER_DETECTION_REGEX = INTERNAL_PREFIX + "([\\w.\\-$]+|\"[^\"]+\"|'[^']+')";

    public static String prefix(final String var) {
        return INTERNAL_PREFIX + Guard.notNull(var);
    }

    public static Pattern prefixPattern() {

        return Pattern.compile(PARAMETER_DETECTION_REGEX);

    }

    public static StatementBuilder from(final String template) {

        return new StatementBuilder(notBlankOrNull(template));

    }

    public static StatementBuilder fromFile(final String template) {

        return new StatementBuilder(readFromPath(notBlankOrNull(template)));

    }

    public static StatementBuilder fromClasspath(final String template) {

        return new StatementBuilder(readFromClasspath(notBlankOrNull(template)));

    }

    public static StatementBuilder extend(final StatementBuilder statementBuilder) {
        // bug, you could change the prefix in the extended version!, also it's even passed at the moment!
        return new StatementBuilder(statementBuilder);

    }

    private final String template;

    private final Map<String,Object> fragments = new HashMap<>();

    private String name = "anonymous/" + UuidUtil.uuid();

    private StatementBuilder(final String template) {

        this.name = notBlankOrNull(name);

        this.template = notBlankOrNull(template);

    }

    private StatementBuilder(final StatementBuilder statementBuilder) {

        this(statementBuilder.template);

        this.fragments.putAll(statementBuilder.fragments);

    }

    public StatementBuilder withName(final String name) {

        // we always tag a uuid on the name for accidental duplicates
        this.name = Guard.notBlankOrNull(name);

        return this;

    }

    public StatementBuilder resolveAsText(final String var, final String string) {

        if (this.fragments.containsKey(var)) throw new UncheckedException("Cannot overwrite the key: " + var);

        this.fragments.put(var, string);

        return this;

    }

    public StatementBuilder resolveAsTextFromFile(final String var, final String path) {

        return this.resolveAsText(var, readFromPath(path));

    }

    public StatementBuilder resolveAsTextFromClasspath(final String var, final String path) {

        return this.resolveAsText(var, readFromClasspath(path));

    }

    public StatementBuilder resolveAsBlank(final String var) {

        if (this.fragments.containsKey(var)) throw new UncheckedException("Cannot overwrite the key: " + var);

        this.fragments.put(var, "");

        return this;

    }

    public StatementBuilder resolveAsInsert(final String colsKey, final String varsKey, final Consumer<InsertBuilder> fn) {

        final InsertBuilder.Impl impl = new InsertBuilder.Impl(this, colsKey, varsKey);

        fn.accept(impl);

        impl.configureStatement();

        return this;

    }

    public StatementBuilder resolveAsSelect(final String selectsKey, final Consumer<SelectBuilder> fn) {

        final SelectBuilder.Impl impl = new SelectBuilder.Impl(this, selectsKey);

        fn.accept(impl);

        impl.configureStatement();

        return this;

    }

    public StatementBuilder resolveAsUpdate(final String colsKey, final Consumer<UpdateBuilder> fn) {

        final UpdateBuilder.Impl impl = new UpdateBuilder.Impl(this, colsKey);

        fn.accept(impl);

        impl.configureStatement();

        return this;

    }

    public StatementBuilder resolveAsWhere(final String whereKey, final Consumer<WhereBuilder> fn) {

        final WhereBuilder.Impl impl = new WhereBuilder.Impl(this, whereKey);

        fn.accept(impl);

        impl.configureStatement();

        return this;

    }

    public StatementBuilder resolveAsOrdering(final String orderKey, final Consumer<OrderingBuilder> fn) {

        final OrderingBuilder.Impl impl = new OrderingBuilder.Impl(this, orderKey);

        fn.accept(impl);

        impl.configureStatement();

        return this;

    }

    public StatementBuilder resolveAsPaging(final String pagingKey, final PagingBuilder.PagingSupport support, final Consumer<PagingBuilder> fn) {

        final PagingBuilder.Impl impl = new PagingBuilder.Impl(this, pagingKey, support);

        fn.accept(impl);

        impl.configureStatement();

        return this;

    }

    public Statement toStatement(final String frameworkPrefix) {

        // in saved sql files sometimes we need to comment a replacement section
        final String decommented = Guard.notNull(this.template).replaceAll("--\\s\\$", "$");

        // build a param string from it
        final ParamString ps = new ParamString(decommented);

        // resolve this with the replacement fragements
        final String sql = ps.resolveWith(this.fragments);

        // extract the params automatically (a v2 feature, uses the special internal prefix)
        final Set<String> params = new HashSet<>(StringUtil.extractParams(prefixPattern().matcher(sql)));

        // switch out the special internal prefix with the desired framework one (jdbi, sql2o, etc)
        final String finalSql = sql.replaceAll(INTERNAL_PREFIX, frameworkPrefix);

        // return it as a statement
        return new Statement(this.name, finalSql, params);

    }

    public ComposedStatementBuilder composeWithPrefix(final String frameworkPrefix) {

        return new ComposedStatementBuilder(this.toStatement(frameworkPrefix));

    }

}
