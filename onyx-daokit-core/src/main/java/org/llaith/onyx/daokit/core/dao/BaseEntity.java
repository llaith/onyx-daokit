package org.llaith.onyx.daokit.core.dao;

import org.llaith.onyx.toolkit.lang.Stringify;

import java.io.Serializable;

/**
 * The only constraint for using the Entity system is the db tables must have an 'id' pseudo-key. They can
 * still keep a compound key as an alternate key, but the dao routines will use the id column.
 */
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    public abstract ID getId(); // generics + reflection in statement requires this approach

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final Object o) {
        return super.equals(o);
    }

    @Override
    public final String toString() {
        return Stringify.toString(this);
    }

}
