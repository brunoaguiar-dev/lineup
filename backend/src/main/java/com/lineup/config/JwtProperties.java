package com.lineup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("lineup.jwt")
public record JwtProperties(String segredo, Duration validadeDoAccessToken) {
}
