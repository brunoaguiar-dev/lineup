package com.lineup.autenticacao;

import com.lineup.config.ApiPaths;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
@Tag(name = "Autenticação")
class AutenticacaoController {

    private final AutenticacaoService service;

    AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @PostMapping("/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return service.logar(request, http.getRemoteAddr());
    }

    @PostMapping("/refresh")
    TokenResponse renovar(@Valid @RequestBody RefreshRequest request) {
        return service.renovar(request.refreshToken());
    }

    @PostMapping("/logout")
    ResponseEntity<Void> sair(@Valid @RequestBody RefreshRequest request) {
        service.sair(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
