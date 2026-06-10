package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * User-defined custom categories, tenant-scoped.
 * Each UserCategory belongs to exactly one AppUser.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(indexes = @Index(name = "idx_user_category_app_user_id", columnList = "app_user_id"))
public class UserCategory extends TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @OneToMany(mappedBy = "userCategory", fetch = FetchType.LAZY)
    private Set<Transaction> transactionSet;

    public static UserCategory ofId(Long id) {
        return UserCategory.builder().id(id).build();
    }
}
