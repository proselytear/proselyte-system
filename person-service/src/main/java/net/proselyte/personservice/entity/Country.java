package net.proselyte.personservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;


@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@NotNull
@Setter
@Getter
@Entity
@Table(name = "countries", schema = "person")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "created", nullable = false)
    private Instant created;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "updated", nullable = false)
    private Instant updated;

    @Size(max = 128)
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Size(max = 3)
    @Column(name = "code", nullable = false, length = 3)
    private String code;
}