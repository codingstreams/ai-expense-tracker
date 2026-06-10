package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Global system-defined categories available to all users.
 * These are seeded at application startup and not owned by any tenant.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "systemCategory", fetch = FetchType.LAZY)
    private Set<Transaction> transactionSet;

    public static SystemCategory ofId(Long id) {
        return SystemCategory.builder().id(id).build();
    }
}
