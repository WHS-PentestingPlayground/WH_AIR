package com.WHS.whair.controller;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@RestController
public class JwkController {

    @Value("classpath:keys/public_key.pem")
    private Resource publicKeyResource;

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwk() throws Exception {
        String pem = new String(publicKeyResource.getInputStream().readAllBytes());
        pem = pem.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(spec);

        RSAKey jwk = new RSAKey.Builder(rsaPublicKey)
                .keyID("wh-key") // 필드명: kid
                .build();

        return ResponseEntity.ok(new JWKSet(jwk).toJSONObject());
    }
}
