package com.WHS.whair.util;

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
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256) // 생성은 여전히 안전하게
                .compact();
    }

    // ❗ JWT 검증 (취약한 버전: alg 필드를 신뢰)
    public String validateAndExtractUsername(String token) {
        try {
            // 주의: setSigningKey만 설정하면 alg를 따로 강제하지 않음!
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey) // 공격자가 HS256이나 none을 사용해도 이 key를 무시할 수 있음
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
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

