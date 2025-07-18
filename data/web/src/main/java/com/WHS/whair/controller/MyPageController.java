package com.WHS.whair.controller;

import com.WHS.whair.dto.MyPageDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.MyPageService;
import com.WHS.whair.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final PasswordUtil passwordUtil;

    // 사용자별 flag 저장 (메모리)
    private final Map<String, String> userFlags = new ConcurrentHashMap<>();

    @CrossOrigin(origins = {
        "http://localhost:8081",
        "http://air.vulunch.kr",
        "https://air.vulunch.kr",
        "http://172.30.0.4:8080"
    })
    @PostMapping("/flag")
    @ResponseBody
    public String receiveFlag(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String password = payload.get("password");
        String flag = payload.get("flag");
        if (userId == null || password == null || flag == null) {
            return "fail";
        }
        try {
            User user = myPageService.getUserInfo(userId);
            if (user == null) {
                System.out.println("[FLAG-POST] fail: user not found");
                return "fail";
            }
            // user.getPasswordHash()는 해시+salt 형태
            if (!passwordUtil.verifyPassword(password, user.getPasswordHash())) {
                System.out.println("[FLAG-POST] fail: password mismatch");
                return "fail";
            }
            userFlags.put(userId, flag);
            System.out.println("[FLAG-POST] success: flag saved for userId=" + userId);
            return "ok";
        } catch (Exception e) {
            System.out.println("[FLAG-POST] exception: " + e.getMessage());
            return "fail";
        }
    }

    @GetMapping
    public String mypage(HttpServletRequest request, Model model) {
        // 세션에서 사용자 객체 가져오기
        User sessionUser = (User) request.getAttribute("user");
        System.out.println("[DEBUG] sessionUser=" + (sessionUser != null ? sessionUser.getName() : "null"));
        System.out.println("[DEBUG] userFlags=" + userFlags);
        
        if (sessionUser == null) {
            return "redirect:/login";
        }

        try {
            // 사용자 정보 조회
            User user = myPageService.getUserInfo(sessionUser.getName());
            model.addAttribute("user", user);

            // 사용자 예약 목록 조회
            List<MyPageDto> reservations = myPageService.getUserReservations(sessionUser.getName());
            model.addAttribute("reservations", reservations);

            // 사용자별 flag 조회
            String flag = userFlags.get(sessionUser.getName());
            if (flag != null && !flag.isEmpty()) {
                model.addAttribute("flag", flag);
            }

            return "mypage";
        } catch (IllegalArgumentException e) {
            return "redirect:/login";
        }
    }
    
}
