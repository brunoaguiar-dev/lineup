package com.lineup.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(TokenProperties.class)
public class TokenConfig {

    @Bean
    SecretKey chaveDeAssinatura(TokenProperties propriedades) {
        byte[] bytes = Base64.getDecoder().decode(propriedades.segredo());
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chave) {
        return NimbusJwtEncoder.withSecretKey(chave).build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey chave) {
        // Sem fixar, o decoder aceita o algoritmo que vier no cabeçalho do token.
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
