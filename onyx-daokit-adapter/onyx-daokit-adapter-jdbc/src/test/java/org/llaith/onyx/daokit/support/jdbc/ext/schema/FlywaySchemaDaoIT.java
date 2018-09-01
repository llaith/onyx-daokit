package org.llaith.onyx.daokit.support.jdbc.ext.schema;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.llaith.onyx.toolkit.results.ResultCount;
import org.llaith.onyx.daokit.ext.schema.FlywaySchemaDao;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

public class FlywaySchemaDaoIT {

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Test
    public void testCreateSchema() {

        DataSource dataSource = newDataSource(
                postgres.getUsername(),
                postgres.getPassword(),
                postgres.getJdbcUrl());

        Flyway flyway = new Flyway();
        flyway.setLocations("db/flyway-schema");
        flyway.setDataSource(dataSource);


        FlywaySchemaDao schemaDao = new FlywaySchemaDao(flyway, "test_schema");

        ResultCount schema = schemaDao.createSchema();

        System.out.println(schema.count());

        Assert.assertEquals("number of flyway files executed was not correct", 2, schema.count());

    }

    @Test
    public void testDropSchema() {

        // need to apply database schema before we can apply the dao schema
        DataSource dataSource = newDataSource(
                postgres.getUsername(),
                postgres.getPassword(),
                postgres.getJdbcUrl());

        Flyway flyway = new Flyway();
        flyway.setLocations("db/flyway-schema");
        flyway.setDataSource(dataSource);


        FlywaySchemaDao schemaDao = new FlywaySchemaDao(flyway, "test_schema2");

        ResultCount schema = schemaDao.createSchema();

        Assert.assertEquals("number of flyway files executed was not correct", 2, schema.count());

        ResultCount resultCount = schemaDao.dropSchema();

        Assert.assertEquals("flyway schema dao was not dropped successfully", 1, resultCount.count());

    }


    private static DataSource newDataSource(final String username, final String password, final String url) {

        final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        return new HikariDataSource(hikariConfig);

    }

}
