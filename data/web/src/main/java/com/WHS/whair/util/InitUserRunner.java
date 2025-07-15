package com.WHS.whair.util;

import com.WHS.whair.dto.RegisterRequestDto;
import com.WHS.whair.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitUserRunner implements ApplicationRunner {

    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!userService.existsByName("wh_manager")) {
                userService.register(new RegisterRequestDto(
                    "wh_manager", "MApasswd!!34", "manager@email.com", "010-0000-0000"));
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
} 