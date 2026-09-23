package com.lineup.usuario;

import com.lineup.config.TokenProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final TokenProperties propriedades;
    private final LimitadorDeTentativas limitador;
    private final RefreshTokenService refreshTokens;

    // Conferido quando o email não existe, para o tempo de resposta ser o mesmo
    // nos dois casos e não revelar quais emails estão cadastrados.
    private final String hashDeComparacao;

    AutenticacaoService(UsuarioRepository repository,
                        PasswordEncoder passwordEncoder,
                        JwtEncoder jwtEncoder,
                        TokenProperties propriedades,
                        LimitadorDeTentativas limitador,
                        RefreshTokenService refreshTokens) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
        this.limitador = limitador;
        this.refreshTokens = refreshTokens;
        this.hashDeComparacao = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    TokenResponse logar(LoginRequest request, String ip) {
        limitador.verificar(ip, request.email());

        Optional<Usuario> encontrado = repository.findByEmailIgnoreCase(request.email())
                .filter(usuario -> usuario.getSenhaHash() != null);

        String hash = encontrado.map(Usuario::getSenhaHash).orElse(hashDeComparacao);
        boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);

        if (!senhaConfere || encontrado.isEmpty()) {
            limitador.registrarFalha(ip, request.email());
            throw new CredenciaisInvalidas();
        }

        limitador.registrarAcerto(ip, request.email());

        Usuario usuario = encontrado.get();
        return responder(usuario, refreshTokens.emitir(usuario, UUID.randomUUID()));
    }

    @Transactional(noRollbackFor = RefreshTokenInvalido.class)
    TokenResponse renovar(String refreshToken) {
        RefreshToken consumido = refreshTokens.consumir(refreshToken);
        Usuario usuario = consumido.getUsuario();

        return responder(usuario, refreshTokens.emitir(usuario, consumido.getFamilia()));
    }

    @Transactional
    void sair(String refreshToken) {
        refreshTokens.revogarSessao(refreshToken);
    }

    private TokenResponse responder(Usuario usuario, String refreshToken) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(propriedades.validadeDoAccessToken());

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("lineup")
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getId().toString())
                .claim("papel", usuario.getPapel().name());

        // Nulo para SUPER_ADMIN, e o JwtClaimsSet recusa claim nula.
        if (usuario.getEscolaId() != null) {
            claims.claim("escolaId", usuario.getEscolaId().toString());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims.build())).getTokenValue();

        return new TokenResponse(token, expiraEm, refreshToken);
    }
}
