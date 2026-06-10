package com.example.et_core.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Base class for entities that need audit fields.
 * Automatically sets createdAt and updatedAt timestamps.
 */
@MappedSuperclass
@Data
public abstract class Auditable {

    private Long createdAt;
    private Long updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
