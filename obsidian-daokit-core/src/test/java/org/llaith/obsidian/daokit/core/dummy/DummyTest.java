package org.llaith.obsidian.daokit.core.dummy;

import org.llaith.obsidian.daokit.core.orm.OrmStatements;
import org.llaith.obsidian.daokit.core.statement.ComposedStatementBuilder;
import org.llaith.obsidian.daokit.core.statement.annotation.Column;
import org.llaith.obsidian.daokit.core.statement.builder.PagingBuilder.PagingSupport;

import java.util.Map;

import static org.llaith.obsidian.daokit.core.statement.StatementBuilder.from;

/**
 *
 */
public class DummyTest {

    private static final OrmStatements statements = OrmStatements.newPostgresStatements();

    @SuppressWarnings({"squid:S1854", "squid:S1481"})
    public static void main(String[] clargs) {

        // TODO: move to test code

        class Entity {

            @Column(invariable = true)
            String id;

            @Column
            String payload1;

            @Column("payload_2")
            String payload2;
        }

        final Map<String,Object> args = null;

        from(statements.insertSql)
                .resolveAsText("table", "notification")
                .resolveAsInsert("cols", "vars", b -> b
                        .addColumnsFrom(Entity.class)
                        .addColumn("from")
                        .addColumn("date"));

        from(statements.selectSql)
                .resolveAsText("table", "notification")
                .resolveAsText("checktable", "notification")
                .resolveAsInsert("cols", "vars", b -> b
                        .addColumn("from")
                        .addColumn("date"))
                .resolveAsWhere("wheres", b -> b
                        .addClause("external_id = @:external_id")
                        .addClause("update_count = @:update_count"));

        from(statements.selectSql)
                .resolveAsText("table", "notification")
                .resolveAsSelect("selects", b -> b
                        .addColumn("from")
                        .addColumn("date"))
                .resolveAsWhere("wheres", b -> b
                        .addClause("external_id = @:external_id")
                        .addClause("update_count = @:update_count"));

        from(statements.selectSql)
                .resolveAsText("table", "notification")
                .resolveAsSelect("selects", b -> b
                        .addColumn("from")
                        .addColumn("date"))
                .resolveAsWhere("wheres", b -> b
                        .addClause("external_id = @:external_id")
                        .addClause("update_count = @:update_count"))
                .resolveAsPaging("paging", PagingSupport.POSTGRES, b -> b.addLimitAndOffset(10, 20));

        from(statements.updateSql)
                .resolveAsText("table", "notification")
                .resolveAsUpdate("cols", b -> b
                        .addColumn("from")
                        .addColumn("date"))
                .resolveAsWhere("wheres", b -> b
                        .addClause("external_id = @:external_id")
                        .addClause("update_count = @:update_count"));

        from(statements.deleteSql)
                .resolveAsText("table", "notification")
                .resolveAsWhere("wheres", b -> b
                        .addClause("external_id = @:external_id")
                        .addClause("update_count = @:update_count"))
                .composeWithPrefix(":")
                .setParameter("col1", null)
                .usingConnection(null)
                .executeQuery(null);

        // another style
        ComposedStatementBuilder.from(
                from(statements.updateSql)
                        .resolveAsText("table", "notification")
                        .resolveAsWhere("wheres", b -> b
                                .addClause("external_id = @:external_id")
                                .addClause("update_count = @:update_count"))
                        .toStatement(":"))
                                .setParameter("col1", null)
                                .usingConnection(null)
                                .executeQuery(null);

    }

}
