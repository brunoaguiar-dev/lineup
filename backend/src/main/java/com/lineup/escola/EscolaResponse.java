package com.lineup.escola;

import java.time.Instant;
import java.util.UUID;

public record EscolaResponse(
        UUID id,
        String nome,
        String telefone,
        String email,
        String endereco,
        String praia,
        String cidade,
        String uf,
        Instant criadoEm,
        Instant atualizadoEm
) {

    static EscolaResponse from(Escola escola) {
        return new EscolaResponse(
                escola.getId(),
                escola.getNome(),
                escola.getTelefone(),
                escola.getEmail(),
                escola.getEndereco(),
                escola.getPraia(),
                escola.getCidade(),
                escola.getUf(),
                escola.getCriadoEm(),
                escola.getAtualizadoEm()
        );
    }
}
