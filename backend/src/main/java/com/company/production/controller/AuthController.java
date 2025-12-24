package com.company.production.controller;

import com.company.production.entity.User;
import com.company.production.repository.RoleRepository;
import com.company.production.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080") // 允许前端Vue项目访问
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        try {
            // 检查用户名是否已存在
            if (userRepository.findByUsername(user.getUsername()) != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户名已存在");
                return errorResponse;
            }

            // 设置角色为普通用户
            user.setRole(roleRepository.findByName("USER"));

            // 密码加密
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // 保存用户
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "注册失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 初始化超级管理员
     * @return 初始化结果
     */
    @PostMapping("/init-admin")
    public Map<String, Object> initAdmin() {
        try {
            // 检查超级管理员是否已存在
            if (userRepository.findByUsername("admin") != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "超级管理员已存在");
                return errorResponse;
            }

            // 创建超级管理员用户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(roleRepository.findByName("ADMIN"));

            // 保存超级管理员
            userRepository.save(admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "超级管理员初始化成功");
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "超级管理员初始化失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 用户登录
     * @param loginRequest 登录请求（包含用户名和密码）
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        // 创建响应头，设置字符集为UTF-8
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        
        try {
            // 获取用户名和密码
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            // 从数据库查询用户
            User user = userRepository.findByUsername(username);
            
            // 验证用户是否存在及密码是否正确
            if (user != null) {
                // 检查密码是否是BCrypt格式
                boolean passwordValid = false;
                String dbPassword = user.getPassword();
                
                if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$")) {
                    // BCrypt格式的密码，使用passwordEncoder验证
                    passwordValid = passwordEncoder.matches(password, dbPassword);
                } else {
                    // 明文密码（仅用于测试），直接比较
                    passwordValid = password.equals(dbPassword);
                }
                
                if (passwordValid) {
                    // 构建响应
                    response.put("success", true);
                    response.put("message", "登录成功");
                    response.put("token", "dummy-token-" + username); // 模拟token
                    response.put("username", username);
                    response.put("role", user.getRole().getName());
                    
                    return ResponseEntity.ok().headers(headers).body(response);
                }
            }
            
            // 用户名或密码错误
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.ok().headers(headers).body(response);
        } catch (Exception e) {
            // 其他错误
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.ok().headers(headers).body(response);
        }
    }
}
