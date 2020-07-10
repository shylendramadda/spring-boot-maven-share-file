package com.techatcore.sharefile.utils;

import com.techatcore.sharefile.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SessionUtil {

    public static User getUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUserDetails) {
            SecurityUserDetails securityUserDetails = (SecurityUserDetails) principal;
            return securityUserDetails.getUser();
        }
        return null;
    }
}
