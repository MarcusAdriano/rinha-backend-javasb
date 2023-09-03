package io.github.marcusadriano.rinhabackend.service.impl;

import io.github.marcusadriano.rinhabackend.dto.api.CreatePessoaRequest;
import io.github.marcusadriano.rinhabackend.dto.api.PessoaResponse;
import io.github.marcusadriano.rinhabackend.repository.PessoaRepository;
import io.github.marcusadriano.rinhabackend.repository.entity.PessoaDocument;
import io.github.marcusadriano.rinhabackend.service.PessoaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PessoaServiceImpl implements PessoaService {

    private final PessoaRepository repository;

    public PessoaServiceImpl(final PessoaRepository repository) {
        this.repository = repository;
    }

    private PessoaDocument parse(final CreatePessoaRequest request) {

        final List<String> safeEmptyStack = request.getStack() == null ? List.of() : request.getStack();

        return PessoaDocument.builder()
                .id(UUID.randomUUID().toString())
                .nome(request.getNome())
                .apelido(request.getApelido())
                .nascimento(request.getNascimento())
                .stack(safeEmptyStack)
                .build();
    }

    private PessoaResponse parse(final PessoaDocument doc) {
        return PessoaResponse.builder()
                .id(doc.getId())
                .nome(doc.getNome())
                .apelido(doc.getApelido())
                .nascimento(doc.getNascimento())
                .stack(doc.getStack())
                .build();
    }

    @Override
    public PessoaResponse create(final CreatePessoaRequest createPessoaRequest) {
        final PessoaDocument pessoaDocument = parse(createPessoaRequest);
        final PessoaDocument doc = repository.save(pessoaDocument);
        return parse(doc);
    }

    @Override
    public Optional<PessoaResponse> findById(final String id) {
        return repository.findById(id).map(this::parse);
    }

    @Override
    public List<PessoaResponse> findByFilter(final String filter) {
        return repository.findAllByPalavraChave(filter).stream().map(this::parse).toList();
    }

    @Override
    public Long count() {
        return repository.count();
    }
}