package org.llaith.onyx.daokit.ext.schema;


import org.llaith.onyx.toolkit.results.ResultCount;

/**
 * Used to create and drop database schemas as required.
 */
public interface SchemaDao {

    /**
     * Create the schema.
     * @return
     */
    ResultCount createSchema();

    /**
     * Drop the schema.
     * @return
     */
    ResultCount dropSchema();
}
