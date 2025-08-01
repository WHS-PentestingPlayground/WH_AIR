package com.WHS.whair.util;

import io.jsonwebtoken.Claims;
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
                .setSubject(username)                           // sub (사용자 식별자)
                .claim("managedBy", "wh_manager")   // 커스텀 클레임(관리자 ID)
                .setIssuedAt(now)                              // iat (발행 시간)
                .setExpiration(expiryDate)                     // exp (만료 시간)
                .signWith(privateKey, SignatureAlgorithm.RS256) // 생성은 여전히 안전하게
                .compact();
    }

    // ❗ JWT 검증 (취약한 버전: alg 필드를 신뢰)
    public Claims parseClaims(String token) {
        try {
            // 알고리즘 혼재 취약점: alg 필드를 신뢰하여 검증
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(3600)  // 시간 오차 허용
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // HS256으로 서명된 토큰도 허용 (알고리즘 혼재 취약점)
            try {
                // 공개키를 HMAC 키로 사용하여 HS256 검증 시도
                return Jwts.parserBuilder()
                        .setSigningKey(publicKey.getEncoded())
                        .setAllowedClockSkewSeconds(3600)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (Exception e2) {
                return null;
            }
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

