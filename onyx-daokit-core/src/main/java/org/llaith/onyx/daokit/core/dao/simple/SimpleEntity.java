/*
 * @Author Nos Doughty
 */
package org.llaith.onyx.daokit.core.dao.simple;

import org.llaith.onyx.daokit.core.statement.annotation.Column;
import org.llaith.onyx.toolkit.results.StaleResultException;
import org.llaith.onyx.daokit.core.dao.BaseEntity;
import org.llaith.onyx.toolkit.util.lang.ToStringUtil;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;
import org.llaith.onyx.toolkit.util.lang.UuidUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

// it's safe to expose this object, and here's why. The id, while leaked, doesn't matter as there are no
// public api calls which take the id as a lookup (only externalids). The audit fields are reset each time.
//
public abstract class SimpleEntity extends BaseEntity<Long> implements Serializable {

    public class Metadata {

        private Integer versionCheck;

        /**
         * Sets the value for setId
         *
         * @param id the param value for
         */
        public void setId(final Long id) {
            SimpleEntity.this.id = id;
        }

        public void setExternalId(final String externalId) {
            SimpleEntity.this.externalId = externalId;
        }

        public void setUpdateCount(final Integer updateCount) {
            SimpleEntity.this.updateCount = updateCount;
        }

        public Metadata init(final Date now) {

            // clear ids - in case we are using on an existing object
            if (SimpleEntity.this.id != null) throw new UncheckedException("Cannot re-init an existing object");

            // init for 'first' save (may be called again on errors saving first time)            
            createDate = now;
            updateDate = now;
            updateCount = 0;
            deleteDate = null;

            // clear misc
            this.versionCheck = null;

            return this;

        }

        public Metadata update(final Date now) {

            if (id == null) throw new UncheckedException("Cannot update a new object");

            this.versionCheck = updateCount;

            updateDate = now;
            updateCount = updateCount + 1;

            return this;

        }

        public Integer versionCheck() {

            final Integer vc = this.versionCheck;

            this.versionCheck = null;

            return vc;

        }

        public void delete(final Date now) {

            // note: only the delete-date, not the update, the update will get called anyway, and we can detect records updated after delete
            deleteDate = now;

        }

    }

    @NotNull
    @Column(invariable = true, auto = true)
    protected Long id;

    @Column
    @NotNull
    @Pattern(regexp = ".*[\\S]+.*")// replacement for @NotBlank
    protected String externalId = UuidUtil.externalId();

    @Column
    @NotNull
    protected Integer updateCount;

    @Column
    @NotNull
    protected Date updateDate;

    @Column(invariable = true)
    @NotNull
    protected Date createDate; // filled on save not object creation!

    @Column
    protected Date deleteDate;

    @ToStringUtil.ToStringIgnore
    private Metadata metadata;

    /**
     * Constructs a new Object
     */
    public SimpleEntity() {
        super();
    }

    public SimpleEntity(final String externalId) {

        super();

        this.externalId = externalId;

    }

    @SuppressWarnings({"unused", "squid:S1694"})
    private Metadata metadata() {

        if (metadata == null) this.metadata = new Metadata();

        return this.metadata;

    }

    @Override
    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return SimpleEntity.this.externalId;
    }

    /**
     * Gets the value for getUpdateCount
     *
     * @return the return value for
     */
    public Integer getUpdateCount() {
        return SimpleEntity.this.updateCount;
    }

    public Date getUpdateDate() {
        return SimpleEntity.this.updateDate;
    }

    public Date getCreateDate() {
        return SimpleEntity.this.createDate;
    }

    public Date getDeleteDate() {
        return SimpleEntity.this.deleteDate;
    }

    public boolean matchesVersion(Integer version) {

        return version != null
                && SimpleEntity.this.updateCount != null
                && version.equals(SimpleEntity.this.updateCount);

    }

    public void versionCheck(final Integer version, final String msg) {

        if (!this.matchesVersion(version)) throw new StaleResultException(msg);

    }

}
