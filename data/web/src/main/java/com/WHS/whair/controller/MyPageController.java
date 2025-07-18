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
            // userId로 저장 (조회 시와 일치시키기 위해)
            userFlags.put(userId, flag);
            System.out.println("[FLAG-POST] success: flag saved for userId=" + userId);
            System.out.println("[FLAG-POST] current userFlags: " + userFlags);
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

            // 사용자별 flag 조회 - 여러 키로 시도
            String flag = userFlags.get(sessionUser.getName());
            if (flag == null && user != null) {
                // sessionUser.getName()으로 찾지 못하면 userId로도 시도
                flag = userFlags.get(String.valueOf(user.getId()));
            }
            if (flag == null) {
                // 모든 키를 로그로 확인
                System.out.println("[DEBUG] Looking for flag with key: " + sessionUser.getName());
                System.out.println("[DEBUG] Available keys in userFlags: " + userFlags.keySet());
            }
            if (flag != null && !flag.isEmpty()) {
                model.addAttribute("flag", flag);
                System.out.println("[DEBUG] Flag found and added to model: " + flag);
            } else {
                System.out.println("[DEBUG] No flag found for user: " + sessionUser.getName());
            }

            return "mypage";
        } catch (IllegalArgumentException e) {
            return "redirect:/login";
        }
    }
    
}
