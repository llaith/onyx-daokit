package org.llaith.obsidian.daokit.support.jdbi.core;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongColumnMapper;
import org.llaith.obsidian.daokit.support.jdbi.core.dao.JdbiDaoActionFactory;
import org.llaith.obsidian.daokit.support.jdbi.core.session.JdbiTransactionControl;
import org.llaith.onyx.toolkit.util.lang.EnumId;
import org.llaith.obsidian.daokit.core.dao.simple.SimpleDao;
import org.llaith.obsidian.daokit.core.dao.simple.SimpleEntity;
import org.llaith.obsidian.daokit.core.orm.OrmBuilder;
import org.llaith.obsidian.daokit.core.orm.OrmStatements;
import org.llaith.obsidian.daokit.core.statement.annotation.Column;
import org.llaith.obsidian.daokit.core.statement.annotation.Converters.EnumConverter;
import org.llaith.obsidian.daokit.core.statement.annotation.Converters.EnumIdConverter;
import org.llaith.obsidian.daokit.core.statement.annotation.Converters.JsonConverter;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.llaith.onyx.toolkit.util.exception.ExceptionUtil.rethrowOrReturn;

/**
 *
 */
public class JdbiConverterTest {

    private static ObjectMapper mapper = new ObjectMapper().registerModule(new ParameterNamesModule());

    private static DataSource dataSource;

    private static JdbiTransactionControl daos;

    static final String createSql =
            "CREATE TABLE IF NOT EXISTS outside (\n" +
                    "  -- metadata\n" +
                    "  id             SERIAL PRIMARY KEY,\n" +
                    "  external_id    TEXT    NOT NULL,\n" +
                    "  update_count   INTEGER NOT NULL DEFAULT 0,\n" +
                    "  update_date    TIMESTAMP WITH TIME ZONE NOT NULL,\n" +
                    "  create_date    TIMESTAMP WITH TIME ZONE NOT NULL,\n" +
                    "  delete_date    TIMESTAMP WITH TIME ZONE,\n" +
                    "  -- payload\n" +
                    "  inside          JSONB NULL," +
                    "  inside_as_str   JSONB NULL," +
                    "  enum1           TEXT NULL," +
                    "  enum2           BIGINT NULL);\n";

    enum Enum1 {
        ONE, TWO;

    }

    enum Enum2 implements EnumId {

        THREE {
            public Long id() {return 123l;}
        },
        FOUR {
            public Long id() {return 456l;}
        };

    }

    static class Outside extends SimpleEntity {

        @Column(converter = JsonConverter.class)
        public Inside inside;

        @Column(converter = JsonConverter.class)
        public String insideAsStr;

        @Column(converter = EnumConverter.class)
        public Enum1 enum1;

        @Column(converter = EnumIdConverter.class)
        public Enum2 enum2;

        public Outside() {
            super();
        }

        public Outside(final String externalId, final Inside inside, final Enum1 enum1, final Enum2 enum2) {
            super(externalId);
            this.inside = inside;
            this.insideAsStr = rethrowOrReturn(() -> mapper.writeValueAsString(inside));
            this.enum1 = enum1;
            this.enum2 = enum2;
        }

    }

    static class Inside {

        @Column
        public String firstName;

        @Column
        public String lastName;

        public Inside() {
        }

        public Inside(final String firstName, final String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            final Inside inside = (Inside)o;

            return new EqualsBuilder()
                    .append(firstName, inside.firstName)
                    .append(lastName, inside.lastName)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(firstName)
                    .append(lastName)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return "Inside{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }

    }

    public static class OutsideDao extends SimpleDao<Handle,Outside> {

        public OutsideDao(final Handle handle) {

            super(
                    new OrmBuilder<>(
                            OrmStatements.newPostgresStatements(),
                            ":",
                            new OrmBuilder.OrmMapping<>(null, "outside", Outside.class)),
                    new JdbiDaoActionFactory().build(Outside.class, LongColumnMapper.WRAPPER),
                    new MetricRegistry(),
                    handle);

        }

    }

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @BeforeClass
    public static void setupJdbi() throws SQLException {

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
        hikariConfig.setUsername(postgres.getUsername());
        hikariConfig.setPassword(postgres.getPassword());

        // init datasource
        dataSource = new HikariDataSource(hikariConfig);

        // create table
        try (java.sql.Statement statement = dataSource.getConnection().createStatement()) {
            statement.execute(createSql);
        }

        // init session
        daos = new JdbiTransactionControl(
                new DBI(dataSource),
                Collections.singletonMap(OutsideDao.class, OutsideDao::new));

    }

    @Test
    public void testAdvancedConversions() throws SQLException {

        final Outside outside = new Outside("0000", new Inside("Mr", "Smith"), Enum1.ONE, Enum2.THREE);

        // save the first one
        final Outside outside1 = daos.with(session -> session
                .use(OutsideDao.class)
                .create(outside)
                .expectResultElseError("Save did not work"));

        // load it back into another
        final Outside outside2 = daos.with(session -> session
                .use(OutsideDao.class)
                .read(outside1.getId())
                .expectNotNull("Could not reload example.")
                .result());

        // check they are equal
        assertTrue("Could not set fields correctly from db", reflectionEquals(
                outside1,
                outside2,
                singletonList("metadata")));

        System.out.println(outside2.inside);
        System.out.println(outside2.insideAsStr);
        System.out.println(outside2.enum1);
        System.out.println(outside2.enum2);

        assertNotNull(outside2.inside);
        assertNotNull(outside2.insideAsStr);
        assertNotNull(outside2.enum1);
        assertNotNull(outside2.enum2);

    }

}
