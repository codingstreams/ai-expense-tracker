package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(indexes = @Index(name = "idx_ai_insight_task_app_user_id", columnList = "app_user_id"))
public class AiInsightTask extends TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InsightType type;

    private String insightText;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}
