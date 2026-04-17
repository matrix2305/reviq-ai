package com.reviq.security.aspect;

import com.reviq.security.annotation.CheckPermission;
import com.reviq.security.annotation.CheckRole;
import com.reviq.security.model.AuthenticatedUser;
import com.reviq.shared.exception.ForbiddenException;
import com.reviq.shared.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Aspect
public class SecurityCheckAspect {

    @Before("@annotation(checkPermission)")
    public void checkPermission(JoinPoint joinPoint, CheckPermission checkPermission) {
        AuthenticatedUser user = getAuthenticatedUser();
        String required = checkPermission.value();

        if (!user.hasPermission(required)) {
            log.warn("AUDIT: Access denied — user '{}' (role={}) lacks permission '{}' for {}.{}",
                    user.getEmail(), user.getRole(), required,
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName());
            throw new ForbiddenException("MISSING_PERMISSION", "Missing permission: " + required);
        }
    }

    @Before("@annotation(checkRole)")
    public void checkRole(JoinPoint joinPoint, CheckRole checkRole) {
        AuthenticatedUser user = getAuthenticatedUser();
        String required = checkRole.value();

        if (user.getRole() == null || !user.getRole().equals(required)) {
            log.warn("AUDIT: Access denied — user '{}' has role '{}', required '{}' for {}.{}",
                    user.getEmail(), user.getRole(), required,
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName());
            throw new ForbiddenException("MISSING_ROLE", "Required role: " + required);
        }
    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("UNAUTHORIZED", "Authentication required");
        }
        return user;
    }
}
