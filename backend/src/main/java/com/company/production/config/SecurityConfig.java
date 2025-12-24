package com.company.production.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级别的权限控制
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    // 自定义过滤器处理模拟的Bearer token
    public class DummyTokenFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
            String authorizationHeader = request.getHeader("Authorization");
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer " + "dummy-token-")) {
                // 提取用户名
                String token = authorizationHeader.substring("Bearer ".length());
                String username = token.substring("dummy-token-".length());
                
                // 创建认证令牌
                List<GrantedAuthority> authorities = new ArrayList<>();
                // 为了简化，我们假设所有使用模拟token的用户都有ROLE_USER权限
                // 实际角色将在AuthenticationService中根据用户信息确定
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    token, null, authorities
                );
                
                // 设置认证信息到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
            chain.doFilter(request, response);
        }
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 禁用CSRF保护
            .cors(withDefaults()) // 启用CORS
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // 允许认证请求匿名访问
                .anyRequest().authenticated() // 要求所有请求都经过认证
            )
            .addFilterBefore(new DummyTokenFilter(), UsernamePasswordAuthenticationFilter.class)
            // .httpBasic(withDefaults()) // 禁用HTTP基本认证
            .formLogin(formLogin -> formLogin.disable()); // 禁用表单登录
        
        return http.build();
    }
}
