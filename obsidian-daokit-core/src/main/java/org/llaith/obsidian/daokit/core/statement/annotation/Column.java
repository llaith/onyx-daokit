package org.llaith.obsidian.daokit.core.statement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Prerequisites: All domain classes must be all final fields (fully immutable) and represent a snapshot of the state
 * of the table. As such, any insert or update that has any DEFAULT (auto) columns *must* reselect on save. For inserts,
 * the INSERT RETURNING syntax should be used, and for updates the UPDATE RETURNING should be used. The original object
 * passed in to the dao should not be used again.
 * <p>
 * Optionality:
 * DB Columns have three column types, nullable, notnull, and default (not-null default and null default are the same
 * iin practice). Additionally they can be immutable which isnt modelled in the db. To account for all these states,
 * we can do the following.
 * DB -> Java
 * NULL -> final
 * NOT NULL -> final @NotNull
 * DEFAULT -> final
 * <p>
 * Mutability:
 * There are 3 kinds of mutablity/supply. We dont yet model supplied by them only (eg, a created date), but if we did,
 * that would be a state something like auto_only instead of auto.
 * Supplied by us+mutable: final
 * Supplied by us, then mutable: final
 * Supplied by us, then immutable: final @Property/readonly
 * Supplied by them, then mutable: final @Property/auto
 * Supplied by them, then immutable: final @Property/readonly @Property/auto
 * <p>
 * Examples:
 * a SERIAL pkey: final @NotNull @Property.readonly=true @Property.auto=true
 * our own pkey or create_date: final @NotNull @Property.readonly=true
 * a consistent id (like batch_id): final @NotNull @Property.readonly=true
 * <p>
 * Incidentally, the Daos could scan the class, and only if they have an auto field, then poison the object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    String value() default "";

    boolean invariable() default false; // set only on inserts

    boolean auto() default false; // will be set for us

    String[] views() default "";

    Class<? extends Converter> converter() default Converters.DefaultConverter.class;

}


