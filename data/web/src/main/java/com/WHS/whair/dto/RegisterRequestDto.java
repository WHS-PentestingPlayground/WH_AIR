package com.WHS.whair.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequestDto {
    private String name;
    private String password;
    private String email;
    private String phoneNumber;
}
