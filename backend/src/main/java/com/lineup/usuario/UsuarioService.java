package com.lineup.usuario;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * A porta do módulo. A entidade e o repositório não saem daqui, e o hash da
 * senha nunca é entregue a quem pergunta.
 */
@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    // Conferido quando o email não existe, para o tempo de resposta ser o mesmo
    // nos dois casos e não revelar quais emails estão cadastrados.
    private final String hashDeComparacao;

    UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.hashDeComparacao = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public Optional<UsuarioAutenticado> autenticar(String email, String senha) {
        Optional<Usuario> encontrado = repository.findByEmailIgnoreCase(email)
                .filter(usuario -> usuario.getSenhaHash() != null);

        String hash = encontrado.map(Usuario::getSenhaHash).orElse(hashDeComparacao);
        boolean senhaConfere = passwordEncoder.matches(senha, hash);

        return encontrado.filter(qualquer -> senhaConfere).map(UsuarioService::autenticado);
    }

    public Optional<UsuarioAutenticado> porId(UUID id) {
        return repository.findById(id).map(UsuarioService::autenticado);
    }

    private static UsuarioAutenticado autenticado(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getPapel(), usuario.getEscolaId());
    }
}
