package org.llaith.obsidian.daokit.core.dao.insertonly;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.llaith.obsidian.daokit.core.statement.annotation.Column;
import org.llaith.obsidian.daokit.core.dao.BaseEntity;
import org.llaith.onyx.toolkit.util.lang.Guard;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 */
public abstract class ImmutableEntity extends BaseEntity<UUID> implements Serializable {

    @Column(invariable = true)
    protected final UUID id;

    @Column(invariable = true, auto = true)
    protected final Date createDate;

    protected ImmutableEntity(final UUID id) {
        this(id, null);
    }

    @JsonCreator
    protected ImmutableEntity(final UUID id, final Date createDate) {
        this.id = Guard.notNull(id);
        this.createDate = createDate;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public Date getCreateDate() {
        return createDate;
    }

}
