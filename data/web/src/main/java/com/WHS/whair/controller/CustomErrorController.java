package com.WHS.whair.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("statusCode", statusCode);
            
            // 에러 메시지 설정
            if (message != null) {
                model.addAttribute("errorMessage", message.toString());
            } else {
                switch (statusCode) {
                    case 404:
                        model.addAttribute("errorMessage", "요청하신 페이지를 찾을 수 없습니다.");
                        break;
                    case 403:
                        model.addAttribute("errorMessage", "접근이 거부되었습니다.");
                        break;
                    case 500:
                        model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
                        break;
                    default:
                        model.addAttribute("errorMessage", "예기치 않은 오류가 발생했습니다.");
                }
            }
        }
        
        if (exception != null) {
            model.addAttribute("exception", exception);
        }
        
        return "error";
    }
}