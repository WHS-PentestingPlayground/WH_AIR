package com.WHS.whair.controller;

import com.WHS.whair.dto.RegisterRequestDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.UserService;
import com.WHS.whair.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 로그인 폼
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String name,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticate(name, password);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");
                return "redirect:/login";
            }

            String token = jwtUtil.generateToken(user.getName());

            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // 실서버는 true
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24); // 24시간
            response.addCookie(cookie);

            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    // 회원가입 폼
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String phoneNumber,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        try {
            RegisterRequestDto dto = new RegisterRequestDto(name, password, email, phoneNumber);
            userService.register(dto);

            // 자동 로그인
            String token = jwtUtil.generateToken(name);
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/";
    }

    // 마이페이지 (JWT에서 사용자 꺼내서 JSP 렌더링)
    @GetMapping("/mypage")
    public String myPage(HttpServletRequest request, Model model) {
        String token = extractTokenFromCookie(request);
        String name = jwtUtil.validateAndExtractUsername(token);
        if (name == null) return "redirect:/login";

        User user = userService.findByName(name);
        model.addAttribute("user", user);
        return "mypage";
    }

    // JWT 토큰 꺼내는 도우미
    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("jwt_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
