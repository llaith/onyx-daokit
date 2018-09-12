package org.llaith.onyx.daokit.support.jdbc.ext.schema;

import com.codahale.metrics.MetricRegistry;
import org.llaith.onyx.toolkit.pattern.results.ResultCount;
import org.llaith.onyx.daokit.support.jdbc.core.statement.JdbcStatementActionFactory;
import org.llaith.onyx.toolkit.lang.Guard;
import org.llaith.onyx.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.daokit.ext.schema.SchemaDao;

import java.sql.Connection;

/**
 *
 */
public class JdbcSchemaDao implements SchemaDao {

    private static final String SCHEMA_ID = "SCHEMA_ID";

    private final String schemaPath;
    private final String schemaId;
    private final MetricRegistry metrics;
    private final Connection connection;
    private final JdbcStatementActionFactory actions;


    public JdbcSchemaDao(final String schemaPath, final String schemaId, final MetricRegistry metrics, final Connection connection) {

        this.schemaPath = Guard.notNull(schemaPath);
        this.schemaId = Guard.notNull(schemaId);
        this.metrics = Guard.notNull(metrics);
        this.connection = Guard.notNull(connection);
        this.actions = new JdbcStatementActionFactory();

    }

    @Override
    public ResultCount createSchema() {

        return StatementBuilder
                .fromClasspath(this.schemaPath)
                .withName("create-schema/" + this.schemaId)
                .resolveAsText(SCHEMA_ID, this.schemaId)
                .composeWithPrefix(":")
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update());

    }

    @Override
    public ResultCount dropSchema() {

        return StatementBuilder
                .from("DROP SCHEMA IF EXISTS ${" + SCHEMA_ID + "} CASCADE;")
                .withName("drop-schema/" + this.schemaId)
                .resolveAsText(SCHEMA_ID, this.schemaId)
                .composeWithPrefix(":")
                .usingConnection(this.connection)
                .instrumentInto(this.metrics)
                .executeUpdate(this.actions.update());

    }

}
