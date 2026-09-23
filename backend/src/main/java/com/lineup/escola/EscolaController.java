package com.lineup.escola;

import com.lineup.config.ApiPaths;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(EscolaController.BASE_PATH)
@Tag(name = "Escolas")
class EscolaController {

    static final String BASE_PATH = ApiPaths.V1 + "/escolas";

    private final EscolaService service;

    EscolaController(EscolaService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ApiResponse(responseCode = "201", description = "Escola criada")
    ResponseEntity<EscolaResponse> criar(@Valid @RequestBody EscolaRequest request,
                                         UriComponentsBuilder uriBuilder) {
        EscolaResponse escola = service.criar(request);
        URI endereco = uriBuilder.path(BASE_PATH + "/{id}").buildAndExpand(escola.id()).toUri();
        return ResponseEntity.created(endereco).body(escola);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@acesso.naEscola(#id, authentication)")
    EscolaResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@acesso.naEscola(#id, authentication)")
    EscolaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody EscolaRequest request) {
        return service.atualizar(id, request);
    }
}
