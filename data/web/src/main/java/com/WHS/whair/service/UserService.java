package com.WHS.whair.service;

import com.WHS.whair.dto.RegisterRequestDto;
import com.WHS.whair.entity.User;
import com.WHS.whair.repository.UserRepository;
import com.WHS.whair.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    @Transactional
    public void register(RegisterRequestDto dto) {
        // 사용자명 중복 검사
        if (userRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }
        
        // manager 이름 사용 금지
       // if ("manager".equals(dto.getName())) {
            //throw new IllegalArgumentException("manager는 사용할 수 없는 이름입니다.");
        //}

        User user = new User();
        user.setName(dto.getName());
        user.setPasswordHash(passwordUtil.hashPassword(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setCreatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    public User authenticate(String name, String password) {
        log.info("🔐 인증 시도: 사용자명={}", name);
        
        User user = userRepository.findByName(name)
                .orElse(null);

        if (user == null) {
            log.warn("❌ 사용자를 찾을 수 없음: 사용자명={}", name);
            return null;
        }

        log.info("✅ 사용자 찾음: ID={}, 이름={}", user.getId(), user.getName());
        
        boolean passwordValid = passwordUtil.verifyPassword(password, user.getPasswordHash());
        log.info("🔑 비밀번호 검증 결과: {}", passwordValid);

        if (!passwordValid) {
            log.warn("❌ 비밀번호 불일치: 사용자명={}", name);
            return null;
        }

        log.info("✅ 인증 성공: 사용자명={}, ID={}", name, user.getId());
        return user;
    }

    public User findByName(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }
}
