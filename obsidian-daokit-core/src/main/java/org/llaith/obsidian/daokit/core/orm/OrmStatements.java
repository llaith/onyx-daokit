package org.llaith.obsidian.daokit.core.orm;

import org.llaith.obsidian.daokit.core.statement.builder.PagingBuilder;
import org.llaith.onyx.toolkit.util.lang.Guard;

/**
 *
 */
public class OrmStatements {

    public static OrmStatements newPostgresStatements() {

        final String insert = "INSERT INTO ${table} (${cols}) VALUES (${vars}) RETURNING ${selects}";

        final String insertConditional =
                "INSERT INTO ${table} (${cols}) " +
                        "SELECT ${vars} " +
                        "WHERE NOT EXISTS (SELECT 1 FROM ${checktable} WHERE 1 = 1 ${wheres}) " +
                        "RETURNING ${cols}";

        final String select = "SELECT ${selects} FROM ${table} WHERE 1 = 1 ${wheres} ${ordering} ${paging}";

        final String update = "UPDATE ${table} SET ${cols} WHERE 1 = 1 ${wheres}";

        final String delete = "DELETE FROM ${table} WHERE 1 = 1 ${wheres}";

        final String listVersioned = "SELECT ${selects} FROM ${table} ${tablealias} LEFT JOIN ${table} b ON (b.supersedes_id = ${tablealias}.id) WHERE b.supersedes_id is null ${wheres} ${ordering} ${paging}";

        return new OrmStatements(
                PagingBuilder.PagingSupport.POSTGRES,
                insert,
                insertConditional,
                select,
                update,
                delete,
                listVersioned);

    }

    public final PagingBuilder.PagingSupport pagingSupport;

    public final String insertSql;
    public final String insertConditionalSql;
    public final String selectSql;
    public final String updateSql;
    public final String deleteSql;
    public final String listVersionedSql;

    public OrmStatements(PagingBuilder.PagingSupport pagingSupport,
                         final String insertSql, final String insertConditionalSql,
                         final String selectSql, final String updateSql, final String deleteSql,
                         final String listVersionedSql) {
        this.pagingSupport = Guard.notNull(pagingSupport);
        this.insertSql = Guard.notBlankOrNull(insertSql);
        this.insertConditionalSql = Guard.notBlankOrNull(insertConditionalSql);
        this.selectSql = Guard.notBlankOrNull(selectSql);
        this.updateSql = Guard.notBlankOrNull(updateSql);
        this.deleteSql = Guard.notBlankOrNull(deleteSql);
        this.listVersionedSql = Guard.notBlankOrNull(listVersionedSql);
    }

}
