package org.llaith.obsidian.daokit.support.jdbi.core.statement;

import org.llaith.onyx.toolkit.results.ResultCount;
import org.llaith.onyx.toolkit.results.ResultList;
import org.llaith.onyx.toolkit.results.ResultObject;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.Update;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.LongColumnMapper;
import org.llaith.onyx.toolkit.util.exception.ThrowableFactory.ExceptionWithoutCause;
import org.llaith.obsidian.daokit.core.statement.ComposedStatement;
import org.llaith.obsidian.daokit.core.statement.ComposedStatementBuilder;
import org.llaith.obsidian.daokit.core.statement.ComposedStatementException;
import org.llaith.obsidian.daokit.core.statement.ResultCountAction;
import org.llaith.obsidian.daokit.core.statement.ResultListAction;
import org.llaith.obsidian.daokit.core.statement.ResultObjectAction;
import org.llaith.obsidian.daokit.core.statement.StatementBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class JdbiStatementActionFactory {

    private static final int DEFAULT_MAX = 10_000;

    private final int max; // max is not the same as a *pagination* limit! Not all frameworks will support a max!

    public JdbiStatementActionFactory() {
        this(DEFAULT_MAX);
    }

    public JdbiStatementActionFactory(final int max) {
        this.max = max;
    }

    public <X> ResultObjectAction<Handle,X> insertAndReturnIdAs(final ResultColumnMapper<X> mapper) {

        return composed -> new ResultObject<>(
                this.buildUpdate(composed)
                    .executeAndReturnGeneratedKeys(mapper)
                    .first(),
                this.exceptionFactory(composed));

    }

    public <X> ResultListAction<Handle,X> queryFor(final ResultSetMapper<X> mapper) {

        return composed -> new ResultList<>(
                this.buildQuery(composed)
                    .map(mapper)
                    .list(this.max),
                this.exceptionFactory(composed));

    }

    public ResultListAction<Handle,Map<String,Object>> query() {

        return composed -> new ResultList<>(
                this.buildQuery(composed)
                    .list(this.max),
                this.exceptionFactory(composed));

    }

    public <X> ResultObjectAction<Handle,X> selectAs(final ResultSetMapper<X> mapper) {

        return composed -> new ResultObject<>(
                this.buildQuery(composed)
                    .map(mapper)
                    .first(),
                this.exceptionFactory(composed));

    }

    public ResultObjectAction<Handle,Map<String,Object>> select() {

        return composed -> new ResultObject<>(
                this.buildQuery(composed)
                    .first(),
                this.exceptionFactory(composed));

    }

    public ResultCountAction<Handle> update() {

        return composed -> new ResultCount(
                this.buildUpdate(composed)
                    .execute(),
                this.exceptionFactory(composed));

    }

    private Query<Map<String,Object>> buildQuery(final ComposedStatement<Handle> composed) {

        final Query<Map<String,Object>> query = composed.connection().createQuery(composed.statement());

        for (final String name : composed.params()) {

            query.bind(name, composed.args().get(name));

        }

        return query;

    }

    private Update buildUpdate(final ComposedStatement<Handle> composed) {

        final Update update = composed.connection().createStatement(composed.statement());

        for (final String name : composed.params()) {

            update.bind(name, composed.args().get(name));

        }

        return update;

    }

    private ExceptionWithoutCause<RuntimeException> exceptionFactory(final ComposedStatement<Handle> composed) {

        return msg -> new ComposedStatementException(composed, msg);

    }

    @SuppressWarnings({"squid:S1481", "squid:S1854", "squid:S2094"})
    public static void main(String[] args) {

        // these are compile checks only at this point.
        final JdbiStatementActionFactory actions = new JdbiStatementActionFactory();
        final Handle connection = null;

        final StatementBuilder verifyLogin = null;
        final StatementBuilder expiringUsers = null;
        final StatementBuilder loadUser = null;
        final StatementBuilder updateName = null;

        class User {
        }
        class UserMapper implements ResultSetMapper<User> {

            @Override
            public User map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException {
                return null;
            }
        }
        
        // not a real test, just looking how the api looks

        final Long id = ComposedStatementBuilder
                .from(verifyLogin.toStatement(":"))
                .setParameter("name", "user1")
                .setParameter("pass", "password1")
                .usingConnection(connection)
                .executeSelect(actions.insertAndReturnIdAs(LongColumnMapper.WRAPPER))
                .expectNotNull("Could not load user")
                .result();

        final List<User> users = ComposedStatementBuilder
                .from(expiringUsers.toStatement(":"))
                .setParameter("expiry", new Date())
                .usingConnection(connection)
                .executeQuery(actions.queryFor(new UserMapper()))
                .expectAtLeastOneResult("No users have expired")
                .asList();

        final List<Map<String,Object>> rawUsers = ComposedStatementBuilder
                .from(expiringUsers.toStatement(":"))
                .setParameter("expiry", new Date())
                .usingConnection(connection)
                .executeQuery(actions.query())
                .expectAtLeastOneResult("No users have expired")
                .asList();

        final User user = ComposedStatementBuilder
                .from(loadUser.toStatement(":"))
                .setParameter("name", "joe")
                .usingConnection(connection)
                .executeSelect(actions.selectAs(new UserMapper()))
                .expectNotNull("User not found")
                .result();

        final Map<String,Object> rawUser = ComposedStatementBuilder
                .from(loadUser.toStatement(":"))
                .setParameter("name", "joe")
                .usingConnection(connection)
                .executeSelect(actions.select())
                .expectNotNull("User not found")
                .result();

        int count = ComposedStatementBuilder
                .from(updateName.toStatement(":"))
                .setParameter("name", "joesmith")
                .usingConnection(connection)
                .executeUpdate(actions.update())
                .expectAtLeastOneCount("Could not update user")
                .count();

    }

}
