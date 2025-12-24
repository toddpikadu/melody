package com.company.production.service;
import com.company.production.entity.User;
import com.company.production.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    /**
     * 检查当前用户是否具有指定角色
     * @param roleName 角色名称
     * @throws SecurityException 如果用户不具有指定角色
     */
    public void checkRole(String... roleNames) throws SecurityException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("用户未认证");
        }

        // 获取当前用户
        User user = getCurrentUser();

        // 检查用户角色
        if (user == null) {
            throw new SecurityException("用户无此权限");
        }
        String userRole = user.getRole().getName();
        for (String roleName : roleNames) {
            if (userRole.equals(roleName)) {
                return;
            }
        }
        throw new SecurityException("用户无此权限");
    }

    /**
     * 获取当前用户
     * @return 当前用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // 获取principal对象
        Object principal = authentication.getPrincipal();
        String username = null;
        
        // 根据principal的类型获取用户名
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            // 如果是字符串类型，尝试从token格式中提取用户名
            String token = (String) principal;
            if (token.startsWith("dummy-token-")) {
                username = token.substring("dummy-token-".length());
            }
        }
        
        // 根据用户名查询用户
        if (username != null) {
            return userRepository.findByUsername(username);
        }
        
        return null;
    }

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRole().getName().equals("admin");
    }
}
