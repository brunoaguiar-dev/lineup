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
        return EscolaResponse.from(repository.saveAndFlush(escola));
    }

    EscolaResponse buscarPorId(UUID id) {
        return EscolaResponse.from(buscarEntidade(id));
    }

    @Transactional
    EscolaResponse atualizar(UUID id, EscolaRequest request) {
        Escola escola = buscarEntidade(id);
        aplicar(request, escola);
        // A entidade já está gerenciada, então o flush basta para o banco gravar
        // e devolver o atualizado_em antes de montarmos a resposta.
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
