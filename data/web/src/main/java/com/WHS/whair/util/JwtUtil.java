package com.WHS.whair.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.expiration}")
    private long expiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            String privateKeyPem = new String(privateKeyResource.getInputStream().readAllBytes());
            privateKey = getPrivateKeyFromPem(privateKeyPem);

            String publicKeyPem = new String(publicKeyResource.getInputStream().readAllBytes());
            publicKey = getPublicKeyFromPem(publicKeyPem);

        } catch (Exception e) {
            throw new RuntimeException("키 초기화 실패", e);
        }
    }

    // JWT 생성 (정상적인 사용자용)
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)                           // sub (사용자 식별자)
                .claim("userId", userId)                        // 사용자 ID
                .claim("managedBy", "wh_manager")   // 커스텀 클레임(관리자 ID)
                .setIssuedAt(now)                              // iat (발행 시간)
                .setExpiration(expiryDate)                     // exp (만료 시간)
                .signWith(privateKey, SignatureAlgorithm.RS256) // 생성은 여전히 안전하게
                .compact();
    }
    
    // 기존 호환성을 위한 메서드
    public String generateToken(String username) {
        // 기존 토큰은 userId 없이 생성
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)                           // sub (사용자 식별자)
                .claim("managedBy", "wh_manager")   // 커스텀 클레임(관리자 ID)
                .setIssuedAt(now)                              // iat (발행 시간)
                .setExpiration(expiryDate)                     // exp (만료 시간)
                .signWith(privateKey, SignatureAlgorithm.RS256) // 생성은 여전히 안전하게
                .compact();
    }

    // ❗ JWT 검증 (취약한 버전: alg 필드를 신뢰)
    public String validateAndExtractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));

            ObjectMapper mapper = new ObjectMapper();
            String alg = mapper.readTree(headerJson).get("alg").asText(); // 🔥 alg 추출

            if ("RS256".equals(alg)) {
                return Jwts.parserBuilder()
                        .setSigningKey(publicKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();

            } else {
                String fakeKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                return Jwts.parserBuilder()
                        .setSigningKey(fakeKey.getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();

            }

        } catch (Exception e) {
            return null;
        }
    }


    // JWT에서 사용자 ID 추출
    public Long extractUserId(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    private PrivateKey getPrivateKeyFromPem(String pem) throws Exception {
        pem = pem.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\r", "")   // \r 제거
                .replaceAll("\\n", "")   // \n 제거
                .replaceAll("\\s+", ""); // 모든 공백 제거
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private PublicKey getPublicKeyFromPem(String pem) throws Exception {
        pem = pem.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\r", "")   // \r 제거
                .replaceAll("\\n", "")   // \n 제거
                .replaceAll("\\s+", ""); // 모든 공백 제거
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }
}

