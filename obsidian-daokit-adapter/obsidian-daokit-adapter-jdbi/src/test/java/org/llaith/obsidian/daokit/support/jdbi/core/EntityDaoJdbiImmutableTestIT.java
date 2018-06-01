package org.llaith.obsidian.daokit.support.jdbi.core;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.llaith.mint.testkit.docker.junit.ext.pgsql.PostgresConfig;
import org.llaith.mint.testkit.docker.junit.ext.pgsql.PostgresResource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.llaith.obsidian.daokit.support.jdbi.core.dao.JdbiDaoActionFactory;
import org.llaith.obsidian.daokit.support.jdbi.core.mapper.UUIDColumnMapper;
import org.llaith.obsidian.daokit.support.jdbi.core.session.JdbiTransactionControl;
import org.llaith.obsidian.daokit.core.dao.insertonly.ImmutableEntity;
import org.llaith.obsidian.daokit.core.dao.insertonly.ImmutableEntityDao;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;
import org.llaith.obsidian.daokit.core.orm.OrmStatements;
import org.llaith.obsidian.daokit.core.statement.annotation.Column;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.llaith.mint.testkit.docker.junit.GenericWaitingStrategies.waitForPort;
import static org.llaith.mint.testkit.docker.junit.ext.pgsql.WaitForPostgresStrategy.waitForSelect;

/**
 *
 */
@SuppressWarnings("Duplicates")
public class EntityDaoJdbiImmutableTestIT {

    @ClassRule
    public static PostgresResource postgres =
            PostgresConfig.builder()
                          .image("postgres:9.6")
                          .postgresPort("5432/tcp")
                          .waitFor(60, 6, 10, (wait) -> {
                              wait.addStrategy(waitForPort("5432/tcp"));
                              wait.addStrategy(waitForSelect("SELECT 1"));
                          })
                          .build();

    private static DataSource dataSource;

    private static JdbiTransactionControl daos;

    @BeforeClass
    public static void setupSql2o() throws SQLException {

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        // init datasource
        dataSource = new HikariDataSource(hikariConfig);

        // create table
        java.sql.Statement statement = dataSource
                .getConnection()
                .createStatement();

        // execute
        statement.execute(Example.createSql);

        // init session
        daos = new JdbiTransactionControl(
                new DBI(dataSource),
                Collections.singletonMap(ExampleDao.class, ExampleDao::new));

    }

    @Test
    public void testBasicConnectionToTestFramework() throws SQLException {

        java.sql.Statement statement = dataSource.getConnection().createStatement();
        statement.execute("SELECT 1");
        ResultSet resultSet = statement.getResultSet();

        resultSet.next();
        int resultSetInt = resultSet.getInt(1);

        Assert.assertEquals("A basic SELECT query succeeds", 1, resultSetInt);

    }

    @Test
    public void testBasicCommitAndCleanup() throws SQLException {

        final Example example = new Example("name", "description");

        // save the first one
        final Example example1 = daos.with(session -> session
                .use(ExampleDao.class)
                .create(example)
                .expectNotNull("Save did not work")
                .result());

        // load it back into another
        final Example example2 = daos.with(session -> session
                .use(ExampleDao.class)
                .read(example1.getId())
                .expectNotNull("Could not reload example.")
                .result());

        // check they are equal
        Assert.assertTrue("Could not set fields correctly from db", EqualsBuilder.reflectionEquals(example1, example2, singletonList("metadata")));

    }

    public static class Example extends ImmutableEntity {

        static final String createSql =
                "CREATE TABLE IF NOT EXISTS example (\n" +
                        "  -- metadata\n" +
                        "  id             UUID PRIMARY KEY,\n" +
                        "  create_date    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),\n" +
                        "  -- payload\n" +
                        "  hump_name        TEXT,\n" +
                        "  dscrptn          TEXT);\n";

        @Column
        public String humpName;

        @Column("dscrptn")
        public String description;

        /**
         * If jackson complains about needing a default ctor and/or a creator, that is probably because
         * the -parameters compiler flag is missing from the ide and/or maven. We are using the ParameterNamesModule
         * in Jackson which pulls the names of the fields from the class file if that parameter has told the compiler
         * to store them. Thanks IBM for ruining that feature.
         */
        public Example(final String humpName, final String description) {
            super(UUID.randomUUID());
            this.humpName = humpName;
            this.description = description;
        }

    }

    public static class ExampleException extends RuntimeException {

        public final UUID id;

        public ExampleException(final UUID id) {
            this.id = id;
        }
    }

    public static class ExampleDao extends ImmutableEntityDao<Handle,Example> {

        public ExampleDao(final Handle handle) {

            super(
                    new OrmBuilder<>(
                            OrmStatements.newPostgresStatements(),
                            ":",
                            new OrmBuilder.OrmMapping<>(null, "example", Example.class)),
                    new JdbiDaoActionFactory().build(Example.class, UUIDColumnMapper.WRAPPER),
                    new MetricRegistry(),
                    handle);

        }

    }

}
