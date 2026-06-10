package com.example.et_core.security;

/**
 * ThreadLocal-based holder for the current tenant (user) ID.
 * Set by JwtAuthFilter after successful authentication, cleared after each request.
 * Used by Hibernate tenant filter to automatically scope queries.
 */
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
