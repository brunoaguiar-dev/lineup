package com.lineup.autenticacao;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Trava a linha: sem isso duas renovações simultâneas com o mesmo token
    // leem usado_em nulo ao mesmo tempo e as duas passam.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken t set t.revogadoEm = :agora
            where t.familia = :familia and t.revogadoEm is null
            """)
    int revogarFamilia(@Param("familia") UUID familia, @Param("agora") Instant agora);
}
