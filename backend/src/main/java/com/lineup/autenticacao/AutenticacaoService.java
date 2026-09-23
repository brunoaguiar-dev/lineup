package com.lineup.autenticacao;

import com.lineup.config.TokenProperties;
import com.lineup.usuario.UsuarioAutenticado;
import com.lineup.usuario.UsuarioService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
class AutenticacaoService {

    private final UsuarioService usuarios;
    private final JwtEncoder jwtEncoder;
    private final TokenProperties propriedades;
    private final LimitadorDeTentativas limitador;
    private final RefreshTokenService refreshTokens;

    AutenticacaoService(UsuarioService usuarios,
                        JwtEncoder jwtEncoder,
                        TokenProperties propriedades,
                        LimitadorDeTentativas limitador,
                        RefreshTokenService refreshTokens) {
        this.usuarios = usuarios;
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
        this.limitador = limitador;
        this.refreshTokens = refreshTokens;
    }

    @Transactional
    TokenResponse logar(LoginRequest request, String ip) {
        limitador.verificar(ip, request.email());

        Optional<UsuarioAutenticado> encontrado =
                usuarios.autenticar(request.email(), request.senha());

        if (encontrado.isEmpty()) {
            limitador.registrarFalha(ip, request.email());
            throw new CredenciaisInvalidas();
        }

        limitador.registrarAcerto(ip, request.email());

        UsuarioAutenticado usuario = encontrado.get();
        return responder(usuario, refreshTokens.emitir(usuario.id(), UUID.randomUUID()));
    }

    @Transactional(noRollbackFor = RefreshTokenInvalido.class)
    TokenResponse renovar(String refreshToken) {
        RefreshToken consumido = refreshTokens.consumir(refreshToken);

        UsuarioAutenticado usuario = usuarios.porId(consumido.getUsuarioId())
                .orElseThrow(RefreshTokenInvalido::new);

        return responder(usuario, refreshTokens.emitir(usuario.id(), consumido.getFamilia()));
    }

    @Transactional
    void sair(String refreshToken) {
        refreshTokens.revogarSessao(refreshToken);
    }

    private TokenResponse responder(UsuarioAutenticado usuario, String refreshToken) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(propriedades.validadeDoAccessToken());

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("lineup")
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.id().toString())
                .claim("papel", usuario.papel().name());

        // Nulo para SUPER_ADMIN, e o JwtClaimsSet recusa claim nula.
        if (usuario.escolaId() != null) {
            claims.claim("escolaId", usuario.escolaId().toString());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();

        return new TokenResponse(token, expiraEm, refreshToken);
    }
}
