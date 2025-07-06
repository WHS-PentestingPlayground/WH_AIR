package com.WHS.whair.controller;

import com.WHS.whair.entity.User;
import com.WHS.whair.service.UserService;
import com.WHS.whair.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class MainController {

  private final UserService userService;
  private final JwtUtil jwtUtil;

  @GetMapping("/")
  public String main(HttpServletRequest request, Model model) {
    String token = extractTokenFromCookie(request);
    if (token != null) {
      String name = jwtUtil.validateAndExtractUsername(token);
      if (name != null) {
        try {
          User currentUser = userService.findByName(name);
          model.addAttribute("user", currentUser);
        } catch (Exception ignored) {
        }
      }
    }
    return "main";
  }

  // 공통 JWT 추출 함수
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
