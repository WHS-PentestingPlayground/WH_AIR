package com.WHS.whair.controller;

import com.WHS.whair.entity.User;
import com.WHS.whair.service.UserService;
import com.WHS.whair.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class MainController {

  @GetMapping("/")
  public String main(HttpServletRequest request, Model model) {
    Object user = request.getAttribute("user");
    if (user != null) {
      model.addAttribute("user", user);  // JSP에서 ${user.name} 가능
    }
    return "main";
  }

}
