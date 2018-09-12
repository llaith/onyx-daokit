package org.llaith.onyx.daokit.core.statement;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.llaith.onyx.toolkit.exception.UncheckedException;
import org.llaith.onyx.toolkit.exception.handler.CompoundExceptionHandler;
import org.llaith.onyx.toolkit.exception.handler.ExceptionHandler;
import org.llaith.onyx.toolkit.lang.Guard;
import org.llaith.onyx.toolkit.pattern.results.ResultCount;
import org.llaith.onyx.toolkit.pattern.results.ResultList;
import org.llaith.onyx.toolkit.pattern.results.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class ComposedStatement<C> {

    private static final Logger logger = LoggerFactory.getLogger(ComposedStatement.class);

    private final C connection;

    private final String statement;

    private final List<String> params = new ArrayList<>();

    private final Map<String,Serializable> args = new HashMap<>();

    private String name;

    private MetricRegistry metrics;

    private ExceptionHandler<RuntimeException> exceptionHandler;

    public ComposedStatement(final C connection, final String statement, final List<String> params, final Map<String,Serializable> args) {

        this(new CompoundExceptionHandler<>(), connection, statement, params, args);

    }

    public ComposedStatement(final ExceptionHandler<RuntimeException> exceptionHandler, final C connection, final String statement, final List<String> params, final Map<String,Serializable> args) {

        this.exceptionHandler = Guard.notNull(exceptionHandler);

        this.connection = Guard.notNull(connection);

        this.statement = Guard.notNull(statement);

        this.params.addAll(Guard.notNull(params));

        this.args.putAll(Guard.notNull(args));

        this.checkParams();

    }

    private void checkParams() {

        final List<String> failed = this.params
                .stream()
                .filter(param -> !param.equals(param.toLowerCase()))
                .collect(Collectors.toList());

        if (!failed.isEmpty()) throw new UncheckedException(
                "Cannot process following params (require lower snake-case): " + failed);

    }

    public C connection() {
        return this.connection;
    }

    public String statement() {
        return this.statement;
    }

    public List<String> params() {
        return this.params;
    }

    public Map<String,Serializable> args() {
        return args;
    }

    public ComposedStatement<C> instrumentInto(final MetricRegistry registry) {

        metrics = Guard.notNull(registry);

        return ComposedStatement.this;

    }

    public <R> ResultObject<R> executeSelect(final ResultObjectAction<C,R> action) {

        this.logSql();

        if (this.metrics == null) return action.apply(this);

        //noinspection unused
        try (final Timer.Context context = startTimer()) {

            return action.apply(this);

        } catch (RuntimeException e) {

            this.exceptionHandler.exceptionCaught(e);

            throw e;

        }

    }

    public <R> ResultList<R> executeQuery(final ResultListAction<C,R> action) {

        this.logSql();

        if (this.metrics == null) return action.apply(this);

        //noinspection unused
        try (final Timer.Context context = startTimer()) {

            return action.apply(this);

        } catch (RuntimeException e) {

            this.exceptionHandler.exceptionCaught(e);

            throw e;

        }

    }

    public ResultCount executeUpdate(final ResultCountAction<C> action) {

        this.logSql();

        if (this.metrics == null) return action.apply(this);

        //noinspection unused
        try (final Timer.Context context = startTimer()) {

            return action.apply(this);

        } catch (RuntimeException e) {

            this.exceptionHandler.exceptionCaught(e);

            throw e;

        }

    }

    private void logSql() {

        logger.debug(String.format(
                "SQL: %s\n Params: %s\n",
                this.statement,
                this.args));

    }

    private Timer.Context startTimer() {

        return this.metrics.timer(MetricRegistry.name(
                ComposedStatement.class,
                this.name)).time();

    }

}
