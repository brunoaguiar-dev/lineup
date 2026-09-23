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

// A escolha por auditoria na aplicação, e não por trigger no banco, está no ADR 0001.
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Sem o updatable = false, o Hibernate inclui criado_em também nos UPDATEs.
    @CreatedDate
    @Column(updatable = false)
    private Instant criadoEm;

    @LastModifiedDate
    private Instant atualizadoEm;
}
