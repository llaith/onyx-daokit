package org.llaith.onyx.daokit.support.jdbc.core.statement;

import org.llaith.onyx.daokit.core.statement.ComposedStatement;
import org.llaith.onyx.daokit.core.statement.ComposedStatementException;
import org.llaith.onyx.daokit.core.statement.ResultCountAction;
import org.llaith.onyx.toolkit.exception.UncheckedException;
import org.llaith.onyx.toolkit.exception.creation.ThrowableFactory.ExceptionWithoutCause;
import org.llaith.onyx.toolkit.pattern.results.ResultCount;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
public class JdbcStatementActionFactory {

    public interface ResultSetMapper<T> {

        T mapResultSet(ResultSet resultSet);

    }

    private static final int DEFAULT_MAX = 10_000;

    private final int max; // max is not the same as a *pagination* limit! Not all frameworks will support a max!

    public JdbcStatementActionFactory() {
        this(DEFAULT_MAX);
    }

    public JdbcStatementActionFactory(final int max) {
        this.max = max;
    }

    public ResultCountAction<Connection> update() {

        return composed -> {

            try {

                final Statement statement = composed.connection().createStatement();
                statement.setMaxRows(this.max);
                statement.execute(composed.statement());

                return new ResultCount(
                        statement.getUpdateCount(),
                        this.exceptionFactory(composed));

            } catch (SQLException e) {
                throw UncheckedException.wrap(e);
            }

        };

    }

    private ExceptionWithoutCause<RuntimeException> exceptionFactory(final ComposedStatement<Connection> composed) {

        return msg -> new ComposedStatementException(composed, msg);

    }

}
