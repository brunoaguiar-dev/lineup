package com.lineup.autenticacao;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Chamado de dentro do @PreAuthorize. Recebe a autenticação por argumento em vez
 * de ler o contexto de segurança, para ser testável sem subir Spring.
 */
@Component("acesso")
public class Acesso {

    private static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    public boolean naEscola(UUID escolaId, Authentication autenticacao) {
        if (autenticacao == null || escolaId == null) {
            return false;
        }
        if (temPapel(autenticacao, SUPER_ADMIN)) {
            return true;
        }
        return escolaId.toString().equals(escolaDoToken(autenticacao));
    }

    private boolean temPapel(Authentication autenticacao, String papel) {
        return autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(papel::equals);
    }

    private String escolaDoToken(Authentication autenticacao) {
        if (autenticacao instanceof JwtAuthenticationToken token) {
            return token.getToken().getClaimAsString("escolaId");
        }
        return null;
    }
}
