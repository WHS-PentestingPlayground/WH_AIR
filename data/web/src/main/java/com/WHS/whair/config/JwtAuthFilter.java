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


import javax.servlet.http.Cookie;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            log.debug("🔍 JWT 토큰 발견: {}", token.substring(0, Math.min(20, token.length())) + "...");
            
            String username = jwtUtil.validateAndExtractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            
            log.debug("👤 JWT에서 추출: username={}, userId={}", username, userId);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("🔐 SecurityContext에 인증 정보 설정: username={}", username);
                UserDetails userDetails = User.builder()
                        .username(username)
                        .password("") // 비밀번호는 필요 없음
                        .authorities(Collections.emptyList())
                        .build();

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            // JWT에서 추출한 정보로 간단한 User 객체 생성 (DB 조회 없이)
            if (username != null) {
                try {
                    com.WHS.whair.entity.User userEntity = new com.WHS.whair.entity.User();
                    if (userId != null) {
                        userEntity.setId(userId);
                    }
                    userEntity.setName(username);
                    request.setAttribute("user", userEntity);
                    log.debug("👤 Request에 사용자 정보 설정: username={}, userId={}", username, userId);
                } catch (Exception ignored) {
                    log.warn("⚠️ 사용자 정보 설정 실패: {}", ignored.getMessage());
                }
            }
        } else {
            log.debug("🔍 JWT 토큰 없음");
        }

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
        
        // 2. 쿠키에서 토큰 찾기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}
