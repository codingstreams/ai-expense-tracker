package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String email;
    private String password;

    @ColumnDefault("false")
    private boolean isOnboardingComplete;

    @Builder.Default
    private Long createdAt = System.currentTimeMillis();
    private Long updatedAt;
    private Long lastLoginAt;
    private Long lastInsightAt;

    @OneToOne(mappedBy = "appUser")
    private UserConfig userConfig;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
    private Set<Account> accountSet;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
    private Set<AiParsingTask> aiParsingTaskSet;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
    private Set<AiInsightTask> aiInsightTaskSet;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
    private Set<Transaction> transactionSet;

    @OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
    private Set<UserCategory> userCategorySet;

    public static AppUser ofId(String appUserId) {
        return AppUser.builder().id(appUserId).build();
    }
}
