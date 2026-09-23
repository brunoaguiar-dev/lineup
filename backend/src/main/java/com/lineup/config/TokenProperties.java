package com.lineup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("lineup.token")
public record TokenProperties(String segredo,
                              Duration validadeDoAccessToken,
                              Duration validadeDoRefreshToken) {
}
