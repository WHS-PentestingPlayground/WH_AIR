package com.WHS.whair.controller;

import com.WHS.whair.dto.MyPageDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public String mypage(HttpSession session, Model model) {
        // 세션에서 사용자 객체 가져오기
        User sessionUser = (User) session.getAttribute("user");
        
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

            return "mypage";
        } catch (IllegalArgumentException e) {
            session.invalidate();
            return "redirect:/login";
        }
    }
}
