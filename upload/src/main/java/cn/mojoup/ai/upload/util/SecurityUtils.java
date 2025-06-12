package cn.mojoup.ai.upload.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

/**
 * Security工具类
 * 
 * @author matt
 */
@Slf4j
public class SecurityUtils {
    
    /**
     * 获取当前登录用户的ID
     * 
     * @return 用户ID，如果未登录则返回"anonymous"
     */
    public static String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                // 如果是匿名用户，返回"anonymous"
                if ("anonymousUser".equals(authentication.getName())) {
                    return "anonymous";
                }
                
                // 返回用户名作为用户ID
                String userId = authentication.getName();
                
                if (StringUtils.hasText(userId)) {
                    log.debug("Current user ID: {}", userId);
                    return userId;
                }
            }
            
            log.debug("No authenticated user found, returning anonymous");
            return "anonymous";
            
        } catch (Exception e) {
            log.warn("Failed to get current user ID, returning anonymous", e);
            return "anonymous";
        }
    }
    
    /**
     * 获取当前登录用户名
     * 
     * @return 用户名，如果未登录则返回"Anonymous User"
     */
    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                
                if (StringUtils.hasText(username) && !"anonymousUser".equals(username)) {
                    return username;
                }
            }
            
            return "Anonymous User";
            
        } catch (Exception e) {
            log.warn("Failed to get current username", e);
            return "Anonymous User";
        }
    }
    
    /**
     * 检查用户是否已认证
     * 
     * @return true如果用户已认证
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null 
                    && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getName());
        } catch (Exception e) {
            log.warn("Failed to check authentication status", e);
            return false;
        }
    }
    
    /**
     * 检查用户是否拥有指定的权限
     * 
     * @param authority 权限名称
     * @return true如果用户拥有该权限
     */
    public static boolean hasAuthority(String authority) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
            }
            
            return false;
            
        } catch (Exception e) {
            log.warn("Failed to check authority: {}", authority, e);
            return false;
        }
    }
} 