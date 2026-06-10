package com.example.et_core.config;

import com.example.et_core.security.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that automatically enables the Hibernate tenant filter
 * for all service-layer method calls when a tenant context is present.
 *
 * This ensures every query on TenantAware entities automatically gets
 * a WHERE app_user_id = :tenantId condition without modifying existing
 * repository methods.
 *
 * The filter is NOT enabled when TenantContext is null (e.g., during
 * batch jobs, scheduled tasks, or unauthenticated requests), allowing
 * system-level operations to query across all tenants.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.example.et_core.service..*(..)) || execution(* com.example.et_core.repo..*(..))")
    public void enableTenantFilter() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter")
                   .setParameter("tenantId", tenantId);
        }
    }
}
