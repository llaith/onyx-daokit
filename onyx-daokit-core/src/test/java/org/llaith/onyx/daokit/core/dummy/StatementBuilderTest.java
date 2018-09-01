package org.llaith.onyx.daokit.core.dummy;

import org.junit.Assert;
import org.junit.Test;
import org.llaith.onyx.daokit.core.dao.simple.SimpleEntity;
import org.llaith.onyx.daokit.core.orm.OrmStatements;
import org.llaith.onyx.daokit.core.statement.Statement;
import org.llaith.onyx.daokit.core.statement.StatementBuilder;
import org.llaith.onyx.daokit.core.statement.annotation.Column;
import org.llaith.onyx.daokit.core.statement.builder.PagingBuilder;

/**
 *
 */
public class StatementBuilderTest {

    private class MyEntity extends SimpleEntity {

        @Column
        public String name;

        @Column
        public String desc;

    }

    private final OrmStatements statements = OrmStatements.newPostgresStatements();

    @Test
    public void testCreate() {

        final Statement sql = StatementBuilder
                .from(this.statements.insertSql)
                .resolveAsText("table", "mytable")
                .resolveAsInsert("cols", "vars", b -> b.addColumnsFrom(MyEntity.class))
                .resolveAsSelect("selects", b -> b.addColumnsFrom(MyEntity.class))
                .toStatement(":");

        System.out.println("sql: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void testReadById() {

        final Statement sql = StatementBuilder
                .from(this.statements.selectSql)
                .resolveAsText("table", "mytable")
                .resolveAsSelect("selects", b -> b.addColumnsFrom(MyEntity.class))
                .resolveAsWhere("wheres", b -> b.addClause("AND id = @:id"))
                .resolveAsBlank("ordering")
                .resolveAsBlank("paging")
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void update() {

        final Statement sql = StatementBuilder
                .from(this.statements.updateSql)
                .resolveAsText("table", "mytable")
                .resolveAsUpdate("cols", b -> b.addColumnsFrom(MyEntity.class))
                .resolveAsWhere("wheres", b -> b.addClause("AND id = @:id"))
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void testDelete() {

        final Statement sql = StatementBuilder
                .from(this.statements.deleteSql)
                .resolveAsText("table", "mytable")
                .resolveAsWhere("wheres", b -> b.addClause("AND id = @:id"))
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void testListAll() {

        final Statement sql = StatementBuilder
                .from(this.statements.selectSql)
                .resolveAsText("table", "mytable")
                .resolveAsSelect("selects", b -> b.addColumnsFrom(MyEntity.class))
                .resolveAsBlank("wheres")
                .resolveAsOrdering("ordering", b -> b.addColumn("create_date", false))
                .resolveAsPaging("paging", PagingBuilder.PagingSupport.POSTGRES, b -> b.addLimitAndOffset(1, 10))
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void testUpdateWithCheck() {

        final Statement sql = StatementBuilder
                .from(this.statements.updateSql)
                .resolveAsText("table", "mytable")
                .resolveAsUpdate("cols", b -> b.addColumnsFrom(MyEntity.class))
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND id = @:id")
                        .addClause("AND update_count = @:optimistic_lock")
                        .addClause("AND delete_date IS NULL"))
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void testDeleteWithCheck() {

        final Statement sql = StatementBuilder
                .from(this.statements.deleteSql)
                .resolveAsText("table", "mytable")
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND id = @:id")
                        .addClause("AND update_count = @:optimistic_lock")
                        .addClause("AND delete_date IS NULL"))
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void testReadByExternalId() {

        final Statement sql = StatementBuilder
                .from(this.statements.selectSql)
                .resolveAsText("table", "mytable")
                .resolveAsSelect("selects", b -> b.addColumnsFrom(MyEntity.class))
                .resolveAsWhere("wheres", b -> b
                        .addClause("AND id = @:id")
                        .addClause("AND delete_date IS NULL"))
                .resolveAsBlank("ordering")
                .resolveAsBlank("paging")
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());

    }

    @Test
    public void testWhereChoices() {

        final Statement sql = StatementBuilder
                .from(this.statements.selectSql)
                .resolveAsText("table", "mytable")
                .resolveAsSelect("selects", b -> b.addColumnsFrom(MyEntity.class))
                .resolveAsWhere("wheres", b -> b.ifNotNullAddClause(null, "WHOOPS1")
                                                .ifNullAddClause(null, "AND id = @:id")
                                                .ifNotNullAddClause("blah", "AND update_level = @:optimistic_lock")
                                                .ifNullAddClause("blah", "WHOOPS2"))
                .resolveAsBlank("ordering")
                .resolveAsBlank("paging")
                .toStatement(":");

        System.out.println("create: " + sql.statement());
        System.out.println("params: " + sql.params());


    }

    @Test
    public void testAssumptionsAboutSqlTemplateRegex() {

        String regex = "--(\\s)*\\$\\{(.*)\\}.*";
        String replace = "\\$\\{$2\\}";

        String test1 = "  --${paging} something1";
        String test2 = "  -- ${paging} something2";
        String test3 = "  -- \t ${paging} something2";

        // we don't care about the leading whitespace, so we trim off for the test, its just there to 
        // test the regex isnt confused by it
        Assert.assertEquals("${paging}", test1.replaceAll(regex, replace).trim());
        Assert.assertEquals("${paging}", test2.replaceAll(regex, replace).trim());
        Assert.assertEquals("${paging}", test3.replaceAll(regex, replace).trim());

    }

}