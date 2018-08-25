package org.llaith.obsidian.daokit.support.jdbi.core;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.llaith.obsidian.daokit.core.dao.simple.SimpleDao;
import org.llaith.obsidian.daokit.core.dao.simple.SimpleEntity;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;
import org.llaith.obsidian.daokit.core.orm.OrmStatements;
import org.llaith.obsidian.daokit.core.statement.annotation.Column;
import org.llaith.obsidian.daokit.support.jdbi.core.dao.JdbiDaoActionFactory;
import org.llaith.obsidian.daokit.support.jdbi.core.session.JdbiTransactionControl;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongColumnMapper;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import static java.util.Collections.singletonList;

/**
 *
 */
@SuppressWarnings("Duplicates")
public class EntityDaoJdbiSimpleTestIT {

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    private static DataSource dataSource;

    private static JdbiTransactionControl daos;

    @BeforeClass
    public static void setupDaos() throws SQLException {

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
    public void testBasicRollback() {

        String externalId = null;

        try {

            daos.with(session -> {

                final Example example = new Example("name", "description");

                session.use(ExampleDao.class).create(example);

                throw new ExampleException(example.getExternalId());

            });

        } catch (final ExampleException e) {

            daos.with(session -> {

                session
                        .use(ExampleDao.class)
                        .readByExternalId(e.externalId)
                        .expectNull("Did not rollback correctly");

                return null;

            });

        }

    }

    @Test
    public void testBasicCommitAndCleanup() {

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

        // then delete and check
        daos.with(session -> session
                .use(ExampleDao.class)
                .delete(example1.getId())
                .expectAtLeastOneCount("Could delete test record.")
                .count());

        // double check we can't load it
        daos.with(session -> session
                .use(ExampleDao.class)
                .read(example1.getId())
                .expectNull("Should not be able to load deleted entity.")
                .result());

        // triple check the table is completely empty
        daos.with(session -> session
                .use(ExampleDao.class)
                .listAll(10)
                .firstResult()
                .expectNull("Should not be able to load deleted entity.")
                .result());

    }

    @Test
    public void testBasicUpdate() {

        final Example example = new Example("name", "description");

        // save the first one
        final Example example1 = daos.with(session -> session
                .use(ExampleDao.class)
                .create(example)
                .expectNotNull("Save did not work")
                .result());

        example1.humpName = "updated";

        daos.with(session -> session
                .use(ExampleDao.class)
                .update(example1)
                .expectAtLeastOneCount("Update failed"));

        // load it back into another
        final Example example2 = daos.with(session -> session
                .use(ExampleDao.class)
                .read(example1.getId())
                .expectNotNull("Could not reload example.")
                .result());

        // check they are equal
        Assert.assertTrue("Could not set fields correctly from db", EqualsBuilder.reflectionEquals(example1, example2, singletonList("metadata")));

        // then delete and check
        daos.with(session -> session
                .use(ExampleDao.class)
                .delete(example1.getId())
                .expectAtLeastOneCount("Could delete test record.")
                .count());

        // double check we can't load it
        daos.with(session -> session
                .use(ExampleDao.class)
                .read(example1.getId())
                .expectNull("Should not be able to load deleted entity.")
                .result());

        // tripple check the table is completely empty
        daos.with(session -> session
                .use(ExampleDao.class)
                .listAll(10)
                .firstResult()
                .expectNull("Should not be able to load deleted entity.")
                .result());

    }

    public static class Example extends SimpleEntity {

        static final String createSql =
                "CREATE TABLE IF NOT EXISTS example (\n" +
                        "  -- metadata\n" +
                        "  id             SERIAL PRIMARY KEY,\n" +
                        "  external_id    TEXT    NOT NULL,\n" +
                        "  update_count   INTEGER NOT NULL DEFAULT 0,\n" +
                        "  update_date    TIMESTAMP WITH TIME ZONE NOT NULL,\n" +
                        "  create_date    TIMESTAMP WITH TIME ZONE NOT NULL,\n" +
                        "  delete_date    TIMESTAMP WITH TIME ZONE,\n" +
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
            this.humpName = humpName;
            this.description = description;
        }

    }

    public static class ExampleException extends RuntimeException {

        public final String externalId;

        public ExampleException(final String externalId) {
            this.externalId = externalId;
        }
    }

    public static class ExampleDao extends SimpleDao<Handle,Example> {

        public ExampleDao(final Handle handle) {

            super(
                    new OrmBuilder<>(
                            OrmStatements.newPostgresStatements(),
                            ":",
                            new OrmBuilder.OrmMapping<>(null, "example", EntityDaoJdbiSimpleTestIT.Example.class)),
                    new JdbiDaoActionFactory().build(Example.class, LongColumnMapper.WRAPPER),
                    new MetricRegistry(),
                    handle);

        }

    }

}
