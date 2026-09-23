package com.lineup.usuario;

public enum Papel {

    /** Opera a plataforma e cria as escolas. Não pertence a nenhuma. */
    SUPER_ADMIN,

    /** O professor. Manda na escola dele e só nela. */
    ADMIN_ESCOLA,

    /** Vê e reserva o que é dele. Para criança, a conta é do responsável. */
    ALUNO
}
