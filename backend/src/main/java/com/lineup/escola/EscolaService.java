package com.lineup.escola;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
class EscolaService {

    private final EscolaRepository repository;

    EscolaService(EscolaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    EscolaResponse criar(EscolaRequest request) {
        Escola escola = new Escola();
        aplicar(request, escola);
        return EscolaResponse.from(repository.save(escola));
    }

    EscolaResponse buscarPorId(UUID id) {
        return EscolaResponse.from(buscarEntidade(id));
    }

    @Transactional
    EscolaResponse atualizar(UUID id, EscolaRequest request) {
        Escola escola = buscarEntidade(id);
        aplicar(request, escola);
        // O @LastModifiedDate é preenchido no @PreUpdate, que o JPA dispara no
        // flush. Sem forçar aqui, a resposta sairia com o valor anterior.
        repository.flush();
        return EscolaResponse.from(escola);
    }

    private Escola buscarEntidade(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EscolaNaoEncontrada(id));
    }

    private void aplicar(EscolaRequest request, Escola escola) {
        escola.setNome(request.nome());
        escola.setTelefone(request.telefone());
        escola.setEmail(request.email());
        escola.setEndereco(request.endereco());
        escola.setPraia(request.praia());
        escola.setCidade(request.cidade());
        escola.setUf(request.uf());
    }
}
