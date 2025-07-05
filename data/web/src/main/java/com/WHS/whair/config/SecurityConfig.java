package com.WHS.whair.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/").permitAll()  // 루트 경로 허용
                .antMatchers("/flights/search").permitAll()  // 항공편 페이지 허용
                .antMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()  // 정적 리소스 허용
                .antMatchers("/favicon.ico").permitAll()  // favicon 허용
                .anyRequest().authenticated()  // 나머지는 인증 필요
            .and()
            .formLogin()
                .disable()  // 기본 로그인 폼 비활성화
            .httpBasic()
                .disable();  // HTTP Basic 인증 비활성화
        
        return http.build();
    }
} 