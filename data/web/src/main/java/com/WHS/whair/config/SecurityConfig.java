package com.WHS.whair.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .antMatchers("/", "/login", "/register", "/logout", "/flights", "/flights/search", "/flights/booking","/flights/api/**", "/mypage").permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()
                .antMatchers("/api/seats/**").permitAll() // 좌석 정보 API는 인증 없이 접근 가능
                .antMatchers("/api/flights/**").permitAll() // 항공편 정보 API도 인증 없이 접근 가능
                .antMatchers("/api/reservations/current-user").authenticated()
                .antMatchers("/api/reservations/**").authenticated() // 예약 관련 API는 인증 필요
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable();
        return http.build();
    }
}

