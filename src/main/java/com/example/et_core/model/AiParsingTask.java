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
@Table(indexes = @Index(name = "idx_ai_parsing_task_app_user_id", columnList = "app_user_id"))
public class AiParsingTask extends TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rawInput;
    private String content;
    private String errorMessage;
    private String correlationId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
}
