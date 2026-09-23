package com.lineup.usuario;

import com.lineup.shared.Auditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
class RefreshToken extends Auditavel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private UUID familia;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiraEm;

    private Instant usadoEm;

    private Instant revogadoEm;

    boolean vencido(Instant agora) {
        return expiraEm.isBefore(agora);
    }

    boolean jaSaiuDeCirculacao() {
        return usadoEm != null || revogadoEm != null;
    }
}
