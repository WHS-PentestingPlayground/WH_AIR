package com.WHS.whair.controller;

import com.WHS.whair.dto.RegisterRequestDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.UserService;
import com.WHS.whair.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestParam String name, @RequestParam String password) {

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "사용자명을 입력해주세요."));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호를 입력해주세요."));
        }

        try {
            User user = userService.authenticate(name.trim(), password);
            
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "아이디 또는 비밀번호가 틀렸습니다."));
            }

            String token = jwtUtil.generateToken(user.getName());

            ResponseCookie cookie = ResponseCookie.from("jwt_token", token)
                    .httpOnly(true)    // 실습용. XSS 방지하려면 true
                    .secure(false)      // HTTPS 사용 시 true
                    .path("/")
                    .maxAge(3600)
                    .build();


            // wh_manager인 경우 manager 페이지로 리다이렉트
            if ("wh_manager".equals(user.getName())) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(Map.of("message", "로그인 성공", "token", token, "redirect", "/manager"));
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("message", "로그인 성공", "token", token));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "로그인 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String phoneNumber,
                           RedirectAttributes redirectAttributes) {

        if (name == null || name.trim().isEmpty()) {
            return redirectWithError("사용자명을 입력해주세요.", redirectAttributes);
        }
        if (password == null || password.trim().isEmpty()) {
            return redirectWithError("비밀번호를 입력해주세요.", redirectAttributes);
        }
        if (email == null || email.trim().isEmpty()) {
            return redirectWithError("이메일을 입력해주세요.", redirectAttributes);
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return redirectWithError("전화번호를 입력해주세요.", redirectAttributes);
        }
        if (password.length() < 8) {
            return redirectWithError("비밀번호는 8자 이상이어야 합니다.", redirectAttributes);
        }
        if (!email.contains("@")) {
            return redirectWithError("올바른 이메일 형식을 입력해주세요.", redirectAttributes);
        }

        try {
            RegisterRequestDto dto = new RegisterRequestDto(name.trim(), password, email.trim(), phoneNumber.trim());
            userService.register(dto);
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            return redirectWithError(e.getMessage(), redirectAttributes);
        } catch (Exception e) {
            return redirectWithError("회원가입 중 오류가 발생했습니다. 다시 시도해주세요.", redirectAttributes);
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)         // 쿠키 삭제
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return "redirect:/";
    }

    private String redirectWithError(String message, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", message);
        return "redirect:/register";
    }
}

