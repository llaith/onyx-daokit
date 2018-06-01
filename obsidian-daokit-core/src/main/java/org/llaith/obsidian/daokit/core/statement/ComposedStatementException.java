package org.llaith.obsidian.daokit.core.statement;

/**
 *
 */
public class ComposedStatementException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ComposedStatementException(final ComposedStatement<?> statement, final String msg) {

        super(String.format(
                "%s.\nStatement: %s\nParameters: %s\nOriginal Exception: %s\n",
                msg,
                statement.statement(),
                statement.args(),
                msg));

    }

    public ComposedStatementException(final ComposedStatement<?> statement, final String msg, final Throwable t) {

        super(String.format(
                "%s.\nStatement: %s\nParameters: %s\nOriginal Exception: %s\n",
                msg,
                statement.statement(),
                statement.args(),
                msg), t);

    }

}
