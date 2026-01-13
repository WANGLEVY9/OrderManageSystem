package com.example.oms.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllers() {
    }

    @Around("restControllers()")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            Object result = pjp.proceed();
            success = true;
            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth != null && auth.isAuthenticated() ? auth.getName() : "anonymous";
                AuditLog log = new AuditLog();
                log.setUsername(username);
                log.setPath(request.getRequestURI());
                log.setHttpMethod(request.getMethod());
                log.setAction(pjp.getSignature().toShortString());
                log.setIp(request.getRemoteAddr());
                log.setSuccess(success);
                log.setDurationMs(duration);
                auditLogRepository.save(log);
            }
        }
    }
}
