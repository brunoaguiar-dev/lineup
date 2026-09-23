package com.lineup.shared;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Identificador e colunas de auditoria, iguais nas tabelas do schema.
 * <p>
 * O listener do Spring Data preenche as duas datas antes da escrita. O mesmo
 * mecanismo traz @CreatedBy quando for preciso registrar quem alterou, o que o
 * banco não tem como saber.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // O @CreatedDate só preenche na criação, mas não impede o Hibernate de
    // incluir a coluna nos UPDATEs seguintes. Quem impede é o updatable = false.
    @CreatedDate
    @Column(updatable = false)
    private Instant criadoEm;

    @LastModifiedDate
    private Instant atualizadoEm;
}
