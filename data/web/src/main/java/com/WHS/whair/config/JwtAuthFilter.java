package com.WHS.whair.config;

import com.WHS.whair.service.UserService;
import com.WHS.whair.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("🔍 [1] JwtAuthFilter invoked for URI: {}", request.getRequestURI());

        String token = extractToken(request);
        log.debug("🔑 [2] Extracted token: {}", token != null ? "exists" : "null");

        if (token != null) {
            String username = jwtUtil.validateAndExtractUsername(token);
            log.debug("✅ [3] Validated username from token: {}", username);

            if (username != null) {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("🛡 [4] No existing authentication found. Setting SecurityContext...");

                    UserDetails userDetails = User.builder()
                            .username(username)
                            .password("") // 비밀번호 필요 없음
                            .authorities(Collections.emptyList())
                            .build();

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("✅ [5] Authentication set in SecurityContext for user: {}", username);
                } else {
                    log.debug("⚠️ [5] SecurityContext already has authentication.");
                }

                try {
                    com.WHS.whair.entity.User userEntity = userService.findByName(username);
                    request.setAttribute("user", userEntity);
                    log.debug("🙋 [6] User entity set in request attribute: {}", userEntity.getName());
                } catch (Exception e) {
                    log.error("❌ [6] Failed to find user entity: {}", e.getMessage());
                }
            } else {
                log.warn("⚠️ [3] Token validation failed. Username is null.");
            }
        } else {
            log.warn("⚠️ [2] No JWT token found in cookies.");
        }

        log.debug("➡️ [7] Passing request to next filter.");
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
