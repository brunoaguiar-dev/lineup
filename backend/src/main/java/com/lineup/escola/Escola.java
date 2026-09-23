package com.lineup.escola;

import com.lineup.shared.Auditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "escola")
@Getter
@Setter
class Escola extends Auditavel {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String endereco;

    @Column(length = 100)
    private String praia;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;
}
