package org.llaith.obsidian.daokit.ext.schema;

import org.flywaydb.core.Flyway;
import org.llaith.onyx.toolkit.results.ResultCount;
import org.llaith.onyx.toolkit.util.lang.Guard;

/**
 * Allows a flyway schema to be created and dropped when required.
 * Flyway files should be templated with ${SCHEMA_ID}.
 */
public class FlywaySchemaDao implements SchemaDao {

    private static final String SCHEMA_ID = "SCHEMA_ID";

    private final Flyway flyway;
    private final String schemaId;

    public FlywaySchemaDao(final Flyway flyway, final String schemaId) {

        this.flyway = flyway;
        this.schemaId = Guard.notNull(schemaId);

        flyway.setSchemas(this.schemaId);
        flyway.getPlaceholders()
                .putIfAbsent(this.SCHEMA_ID, this.schemaId);

    }


    public ResultCount createSchema() {

        int migrate = flyway.migrate();

        return new ResultCount(migrate);

    }

    public ResultCount dropSchema() {

        flyway.clean();

        return new ResultCount(1);

    }
}
