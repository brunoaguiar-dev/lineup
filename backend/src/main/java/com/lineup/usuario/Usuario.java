package com.lineup.usuario;

import com.lineup.shared.Auditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "usuario")
@Getter
@Setter
class Usuario extends Auditavel {

    // Referência por id, e não @ManyToOne: módulos não se enxergam por objeto.
    @Column(name = "escola_id")
    private UUID escolaId;

    @Column(nullable = false, length = 150)
    private String email;

    // Nulo até o usuário abrir o link de ativação e definir a senha.
    @Column(length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;
}
