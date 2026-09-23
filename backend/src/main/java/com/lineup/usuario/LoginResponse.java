package com.lineup.usuario;

import java.time.Instant;

public record LoginResponse(String accessToken, Instant expiraEm) {
}
