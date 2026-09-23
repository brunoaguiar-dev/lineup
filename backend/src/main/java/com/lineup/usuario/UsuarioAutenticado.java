package com.lineup.usuario;

import java.util.UUID;

public record UsuarioAutenticado(UUID id, Papel papel, UUID escolaId) {
}
