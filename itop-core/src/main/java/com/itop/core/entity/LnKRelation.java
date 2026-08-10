package com.itop.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Link - Generic relationship between CIs
 * Used to model dependencies, connections, impacts, etc.
 */
@Entity
@Table(name = "lnk_ci_relation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AssociationOverrides({
    @AssociationOverride(name = "sourceCI", joinColumns = @JoinColumn(name = "source_ci_id")),
    @AssociationOverride(name = "targetCI", joinColumns = @JoinColumn(name = "target_ci_id"))
})
public class LnKRelation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_ci_id", nullable = false)
    private ConfigurationItem sourceCI;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_ci_id", nullable = false)
    private ConfigurationItem targetCI;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 50)
    private RelationType relationType;

    @Column(name = "relation_strength", length = 20)
    private String relationStrength = "medium";

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    public enum RelationType {
        DEPENDS_ON,
        CONNECTS_TO,
        MANAGES,
        HOSTS,
        USES,
        IMPLEMENTS,
        BACKS_UP,
        IMPACTS
    }
}