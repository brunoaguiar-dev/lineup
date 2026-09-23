package com.lineup.usuario;

import com.lineup.config.JwtProperties;
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
    private final JwtProperties propriedades;
    private final LimitadorDeTentativas limitador;

    // Conferido quando o email não existe, para o tempo de resposta ser o mesmo
    // nos dois casos e não revelar quais emails estão cadastrados.
    private final String hashDeComparacao;

    AutenticacaoService(UsuarioRepository repository,
                        PasswordEncoder passwordEncoder,
                        JwtEncoder jwtEncoder,
                        JwtProperties propriedades,
                        LimitadorDeTentativas limitador) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
        this.limitador = limitador;
        this.hashDeComparacao = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    LoginResponse logar(LoginRequest request, String ip) {
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
        return emitirToken(encontrado.get());
    }

    private LoginResponse emitirToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(propriedades.validadeDoAccessToken());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("lineup")
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getId().toString())
                .claim("papel", usuario.getPapel().name())
                // Nulo para SUPER_ADMIN.
                .claim("escolaId", usuario.getEscolaId())
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();

        return new LoginResponse(token, expiraEm);
    }
}
