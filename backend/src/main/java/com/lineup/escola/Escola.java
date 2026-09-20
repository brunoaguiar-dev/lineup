package com.lineup.escola;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "escola")
@Getter
@Setter
class Escola {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    // Quem escreve essas duas colunas é o banco, e o @Generated faz o Hibernate
    // reler o valor depois de gravar. Para criado_em ele cobre só o INSERT, então
    // o updatable = false é que impede a coluna de ser reescrita.
    @Generated(event = EventType.INSERT)
    @Column(updatable = false)
    private Instant criadoEm;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    private Instant atualizadoEm;
}
