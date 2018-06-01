package org.llaith.obsidian.daokit.core.dao.insertonly;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.llaith.obsidian.daokit.core.statement.annotation.Column;
import org.llaith.onyx.toolkit.util.exception.UncheckedException;
import org.llaith.onyx.toolkit.util.lang.StringUtil;
import org.llaith.onyx.toolkit.util.reflection.AnnotationUtil;
import org.llaith.obsidian.daokit.core.dao.BaseEntity;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.llaith.onyx.toolkit.util.lang.StringUtil.notBlankOrNull;

/**
 *
 */
public abstract class VersionedEntity extends BaseEntity<Long> implements Serializable {

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ConsistentId {

        // if set, is used as name of column, else fieldname is used
        String value() default "";
    }

    @NotNull
    @Column(invariable = true, auto = true)
    private Long id;

    @Column(invariable = true)
    private Long supersedesId;

    @NotNull
    @Column(invariable = true, auto = true)
    private Date createDate;

    private final ConsistentIdScanner consistentIdScanner = new ConsistentIdScanner(this.getClass());

    public VersionedEntity() {
        super();
        // used by normal subclasses
    }

    @JsonCreator
    protected VersionedEntity(final Long id, final Long supersedesId, final Date createDate) {
        this.id = id;
        this.supersedesId = supersedesId;
        this.createDate = createDate;
    }

    protected void setSupersedes(final Long id) {

        this.supersedesId = id;

    }

    @Override
    public Long getId() {
        return id;
    }

    public Long getSupersedesId() {
        return supersedesId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getConsistentIdName() {

        return this.consistentIdScanner.expectConsistentIdName();

    }

    public static class ConsistentIdScanner {

        private final Class<?> klass;

        private String consistentIdName = null;

        public ConsistentIdScanner(final Class<?> klass) {
            this.klass = klass;
        }

        public String expectConsistentIdName() {

            final String name = this.getConsistentIdName();

            if (name == null) throw new UncheckedException(String.format(
                    "Error when scanning class: %s as there is no field marked with @ConsistentId",
                    this.klass.getName()));

            return name;

        }

        public String getConsistentIdName() {

            if (consistentIdName == null) {

                final List<Field> consistentIds = AnnotationUtil.fieldsMarkedWith(this.klass, ConsistentId.class);

                if (consistentIds.isEmpty()) return null;

                if (consistentIds.size() > 1) throw new UncheckedException(String.format(
                        "Error when scanning class: %s as the fields [%s] were found instead of a single field marked as @ConsistentId",
                        this.klass.getName(),
                        consistentIds));

                final Field field = consistentIds.get(0);

                if (notBlankOrNull(field.getAnnotation(ConsistentId.class).value()))
                    return field.getAnnotation(ConsistentId.class).value();

                if (field.isAnnotationPresent(Column.class) && notBlankOrNull(field.getAnnotation(ConsistentId.class).value()))
                    return field.getAnnotation(Column.class).value();

                return StringUtil.camelCaseToSnakeCase(field.getName());

            }

            return consistentIdName;

        }

    }

}
