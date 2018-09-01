package org.llaith.onyx.daokit.core.orm;

import org.llaith.onyx.daokit.core.dao.insertonly.VersionedEntity;
import org.llaith.onyx.daokit.core.statement.Statement;
import org.llaith.onyx.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.daokit.core.statement.annotation.PropertyUtil;
import org.llaith.onyx.daokit.core.statement.builder.WhereBuilder;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;
import org.llaith.onyx.toolkit.util.lang.StringUtil;

import java.util.function.Consumer;

import static java.lang.String.format;
import static org.llaith.onyx.toolkit.util.lang.Guard.notBlankOrNull;
import static org.llaith.onyx.toolkit.util.lang.Guard.notNull;

/**
 *
 */
public class OrmBuilder<T> {

    public static class Factory {

        private final OrmStatements statements;

        private final String prefix;

        public Factory(final OrmStatements statements, final String prefix) {

            this.statements = notNull(statements);

            this.prefix = notNull(prefix);

        }

        public <T> OrmBuilder<T> newBuilder(final OrmMapping<T> mapping) {

            return new OrmBuilder<>(this.statements, this.prefix, mapping);

        }

    }

    /**
     *
     */
    public static class OrmMapping<T> {

        public final String schema;
        public final String table;
        public final Class<T> klass;

        public final String fqn;

        public final String consistentIdName;

        public OrmMapping(final String schema, final String table, final Class<T> klass) {

            this.schema = schema;

            this.table = notBlankOrNull(table);

            this.klass = notNull(klass);

            this.fqn = StringUtil.notBlankOrNull(schema) ?
                    format("%s.%s", schema, table) :
                    table;

            this.consistentIdName = new VersionedEntity.ConsistentIdScanner(klass).getConsistentIdName();

        }

    }

    private final OrmStatements statements;

    private final String prefix;

    private final OrmMapping<T> mapping;

    public OrmBuilder(final OrmStatements statements, final String prefix, final OrmMapping<T> mapping) {

        this.statements = notNull(statements);

        this.prefix = notNull(prefix);

        this.mapping = notNull(mapping);

    }

    public OrmMapping<T> getMapping() {

        return this.mapping;

    }

    public String getConsistentIdName() {

        return this.mapping.consistentIdName;

    }

    public String expectConsistentIdName() {

        if (this.mapping.consistentIdName == null) throw new UncheckedException(
                "Missing @ConsistentId on class: " + this.mapping.klass.getName());

        return this.mapping.consistentIdName;

    }

    public Statement createInsertSql() {

        return StatementBuilder
                .from(this.statements.insertSql)
                .withName(this.mapping.fqn + "-insert")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsInsert("cols", "vars", b -> b.addColumnsFrom(this.mapping.klass, PropertyUtil.PropertyAccess.EXCLUDE_INVARIANT_PLUS_AUTO))
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .toStatement(this.prefix);

    }

    public Statement createInsertConditionallySql(final String checkTable, final Consumer<WhereBuilder> whereBuilder) {

        return StatementBuilder
                .from(this.statements.insertConditionalSql)
                .withName(this.mapping.fqn + "-insert-conditionally")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsText("checktable", checkTable)
                .resolveAsInsert("cols", "vars", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", whereBuilder)
                .toStatement(this.prefix);

    }

    public Statement createInsertVersionedSql() {

        return StatementBuilder
                .from(this.statements.insertSql)
                .withName(this.mapping.fqn + "-revise")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsInsert("cols", "vars", b -> b.addColumnsFrom(this.mapping.klass, PropertyUtil.PropertyAccess.EXCLUDE_INVARIANT))
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .toStatement(this.prefix);

    }

    public Statement createLoadSql() {

        return StatementBuilder
                .from(this.statements.selectSql)
                .withName(this.mapping.fqn + "-read")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", b -> b.addClause("AND id = @:id"))
                .resolveAsBlank("ordering")
                .resolveAsBlank("paging")
                .toStatement(this.prefix);

    }

    public Statement createLoadByExternalIdSql() {

        return StatementBuilder
                .from(this.statements.selectSql)
                .withName(this.mapping.fqn + "-read-by-external-id")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND external_id = @:external_id")
                        .addClause("AND delete_date IS NULL"))
                .resolveAsBlank("ordering")
                .resolveAsBlank("paging")
                .toStatement(this.prefix);

    }

    public Statement createLoadByExternalIdWithCheckSql() {

        return StatementBuilder
                .from(this.statements.selectSql)
                .withName(this.mapping.fqn + "-read-by-external-id")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND external_id = @:external_id")
                        .addClause("AND update_count = @:optimistic_lock")
                        .addClause("AND delete_date IS NULL"))
                .resolveAsBlank("ordering")
                .resolveAsBlank("paging")
                .toStatement(this.prefix);

    }

    public Statement createLoadVersionedSql() {

        // technically doesn't need to be a VersionedEntity, but must have the @ConsistentId field
        final String consistentId = this.expectConsistentIdName();

        return StatementBuilder
                .from(statements.listVersionedSql)
                .withName(this.mapping.fqn + "-read-versioned")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsText("tablealias", "a")
                .resolveAsSelect("selects", b -> b.addColumnsFrom("a", this.mapping.klass))
                .resolveAsWhere("wheres", b -> b.addClause(String.format("AND a.%s = %s", consistentId, StatementBuilder.prefix(consistentId))))
                .resolveAsBlank("ordering")
                .resolveAsBlank("paging")
                .toStatement(this.prefix);

    }

    public Statement createListVersionedHistorySql(final int limit) {

        // technically doesn't need to be a VersionedEntity, but must have the @ConsistentId field
        final String consistentId = this.expectConsistentIdName();

        return StatementBuilder
                .from(this.statements.selectSql)
                .withName(this.mapping.fqn + "-list-history")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", b -> b.addClause(String.format("AND %s = %s", consistentId, StatementBuilder.prefix(consistentId))))
                .resolveAsOrdering("ordering", b -> b.addColumn("create_date", false))
                .resolveAsPaging("paging", this.statements.pagingSupport, b -> b.addLimit(limit))
                .toStatement(this.prefix);

    }

    public Statement createListAllSql(final int limit) {

        return StatementBuilder
                .from(this.statements.selectSql)
                .withName(this.mapping.fqn + "-list-all")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsBlank("wheres")
                .resolveAsOrdering("ordering", b -> b.addColumn("create_date", false))
                .resolveAsPaging("paging", this.statements.pagingSupport, b -> b.addLimit(limit))
                .toStatement(this.prefix);

    }

    public Statement createListAllVersionedSql(final String tablealias, final int limit) {

        return StatementBuilder
                .from(statements.listVersionedSql)
                .withName(this.mapping.fqn + "-list-all-versioned")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsText("tablealias", tablealias)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(tablealias, this.mapping.klass))
                .resolveAsBlank("wheres")
                .resolveAsOrdering("ordering", b -> b.addColumn(format("%s.create_date", tablealias), false))
                .resolveAsPaging("paging", this.statements.pagingSupport, b -> b.addLimit(limit))
                .toStatement(this.prefix);

    }

    public Statement createListAllWithOffsetSql(final int limit, final int offset) {

        return StatementBuilder
                .from(this.statements.selectSql)
                .withName(this.mapping.fqn + "-list-all-offset")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsBlank("wheres")
                .resolveAsOrdering("ordering", b -> b.addColumn("create_date", false))
                .resolveAsPaging("paging", this.statements.pagingSupport, b -> b.addLimitAndOffset(limit, offset))
                .toStatement(this.prefix);

    }

    public Statement createListAllVersionedWithOffsetSql(final String tablealias, final int limit, final int offset) {

        return StatementBuilder
                .from(statements.listVersionedSql)
                .withName(this.mapping.fqn + "-list-all-versioned-offset")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsText("tablealias", tablealias)
                .resolveAsSelect("selects", b -> b.addColumnsFrom(tablealias, this.mapping.klass))
                .resolveAsBlank("wheres")
                .resolveAsOrdering("ordering", b -> b.addColumn(format("%s.create_date", tablealias), false))
                .resolveAsPaging("paging", this.statements.pagingSupport, b -> b.addLimitAndOffset(limit, offset))
                .toStatement(this.prefix);

    }


    public Statement createUpdateSql() {

        return StatementBuilder
                .from(this.statements.updateSql)
                .withName(this.mapping.fqn + "-update")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsUpdate("cols", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", b -> b.addClause("AND id = @:id"))
                .toStatement(this.prefix);

    }

    public Statement createUpdateWithCheckSql() {

        return StatementBuilder
                .from(this.statements.updateSql)
                .withName(this.mapping.fqn + "-update-use-check")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsUpdate("cols", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND id = @:id")
                        .addClause("AND update_count = @:optimistic_lock")
                        .addClause("AND delete_date IS NULL"))
                .toStatement(this.prefix);

    }

    public Statement createUpdateByExternalIdWithCheckSql() {

        return StatementBuilder
                .from(this.statements.updateSql)
                .withName(this.mapping.fqn + "-update-by-external-id-use-check")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsUpdate("cols", b -> b.addColumnsFrom(this.mapping.klass))
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND external_id = @:external_id")
                        .addClause("AND update_count = @:optimistic_lock")
                        .addClause("AND delete_date IS NULL"))
                .toStatement(this.prefix);

    }

    public Statement createDeleteSql() {

        return StatementBuilder
                .from(this.statements.deleteSql)
                .withName(this.mapping.fqn + "-delete")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsWhere("wheres", b -> b.addClause("AND id = @:id"))
                .toStatement(this.prefix);

    }

    public Statement createDeleteWithCheckSql() {

        return StatementBuilder
                .from(this.statements.deleteSql)
                .withName(this.mapping.fqn + "-delete")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND id = @:id")
                        .addClause("AND update_count = @:optimistic_lock")
                        .addClause("AND delete_date IS NULL"))
                .toStatement(this.prefix);

    }

    public Statement createDeleteByExternalIdWithCheckSql() {

        return StatementBuilder
                .from(this.statements.deleteSql)
                .withName(this.mapping.fqn + "-delete-by-external-id-use-check")
                .resolveAsText("table", this.mapping.fqn)
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND external_id = @:external_id")
                        .addClause("AND update_count = @:optimistic_lock")
                        .addClause("AND delete_date IS NULL"))
                .toStatement(this.prefix);

    }

}
