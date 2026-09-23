package com.lineup.autenticacao;

import java.time.Instant;

public record TokenResponse(String accessToken, Instant expiraEm, String refreshToken) {
}
