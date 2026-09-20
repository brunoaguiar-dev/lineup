package com.lineup.escola;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EscolaRequest(

        @NotBlank
        @Size(max = 150)
        String nome,

        @Size(max = 20)
        String telefone,

        @Email
        @Size(max = 150)
        String email,

        @Size(max = 255)
        String endereco,

        @Size(max = 100)
        String praia,

        @Size(max = 100)
        String cidade,

        @Size(min = 2, max = 2)
        String uf
) {
}
