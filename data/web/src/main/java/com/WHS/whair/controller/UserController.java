package com.WHS.whair.controller;

import com.WHS.whair.dto.RegisterRequestDto;
import com.WHS.whair.dto.MyPageDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.UserService;
import com.WHS.whair.service.MyPageService;
import com.WHS.whair.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MyPageService myPageService;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestParam String name,
                                   @RequestParam String password,
                                   HttpServletRequest request) {

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
            request.getSession().setAttribute("user", user);//세션에도 저장
            return ResponseEntity.ok(Map.of("token", token));

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
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/";
    }

    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String name = userDetails.getUsername();

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

    private String redirectWithError(String message, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", message);
        return "redirect:/register";
    }
}

