package com.WHS.whair.controller;

import com.WHS.whair.dto.RegisterRequestDto;
import com.WHS.whair.dto.MyPageDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.UserService;
import com.WHS.whair.service.MyPageService;
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
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MyPageService myPageService;
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
        
        // 입력값 검증
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "사용자명을 입력해주세요.");
            return "redirect:/login";
        }
        
        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "비밀번호를 입력해주세요.");
            return "redirect:/login";
        }
        
        try {
            User user = userService.authenticate(name.trim(), password);
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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "로그인 중 오류가 발생했습니다. 다시 시도해주세요.");
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
        
        // 입력값 검증
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "사용자명을 입력해주세요.");
            return "redirect:/register";
        }
        
        if (password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "비밀번호를 입력해주세요.");
            return "redirect:/register";
        }
        
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "이메일을 입력해주세요.");
            return "redirect:/register";
        }
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "전화번호를 입력해주세요.");
            return "redirect:/register";
        }
        
        // 비밀번호 길이 검증
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "비밀번호는 8자 이상이어야 합니다.");
            return "redirect:/register";
        }
        
        // 이메일 형식 검증 (간단한 검증)
        if (!email.contains("@")) {
            redirectAttributes.addFlashAttribute("error", "올바른 이메일 형식을 입력해주세요.");
            return "redirect:/register";
        }
        
        try {
            RegisterRequestDto dto = new RegisterRequestDto(name.trim(), password, email.trim(), phoneNumber.trim());
            userService.register(dto);

            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
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

        try {
            User user = myPageService.getUserInfo(name);
            List<MyPageDto> reservations = myPageService.getUserReservations(name);
            
            model.addAttribute("user", user);
            model.addAttribute("reservations", reservations);
            return "mypage";
        } catch (Exception e) {
            return "redirect:/login";
        }
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
