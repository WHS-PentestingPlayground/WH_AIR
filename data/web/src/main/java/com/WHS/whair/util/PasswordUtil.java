package com.WHS.whair.util;

import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    // 비밀번호 해싱 (SHA-256 + Salt)
    public String hashPassword(String password) {
        try {
            String salt = generateSalt();
            String saltedPassword = salt + password;
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            
            return salt + ":" + Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해싱 알고리즘 오류", e);
        }
    }

    // 비밀번호 검증
    public boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            String salt = parts[0];
            String storedPasswordHash = parts[1];
            
            String saltedPassword = salt + password;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            String inputHash = Base64.getEncoder().encodeToString(hashedBytes);
            
            return storedPasswordHash.equals(inputHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해싱 알고리즘 오류", e);
        }
    }

    // Salt 생성
    private String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
} 