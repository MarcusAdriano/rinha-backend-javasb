package io.github.marcusadriano.rinhabackend.service.impl;

import com.mongodb.ReadConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import io.github.marcusadriano.rinhabackend.dto.api.CreatePessoaRequest;
import io.github.marcusadriano.rinhabackend.dto.api.PessoaResponse;
import io.github.marcusadriano.rinhabackend.service.PessoaService;
import io.github.marcusadriano.rinhabackend.utils.DateUtils;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class PessoaServiceImpl implements PessoaService {

    private final MongoDatabase db;

    public PessoaServiceImpl(final MongoClient mongoClient) {
        this.db = mongoClient.getDatabase("rinchabackend2023-q3");
    }

    @Override
    public String create(final CreatePessoaRequest createPessoaRequest) {

        HashSet<String> stack = null;
        if (createPessoaRequest.getStack() != null) {
            stack = new HashSet<>(createPessoaRequest.getStack());
        }

        final var id = UUID.randomUUID().toString();

        final var doc = new Document();
        doc.put("_id", id);
        doc.put("nome", createPessoaRequest.getNome().trim());
        doc.put("apelido", createPessoaRequest.getApelido().trim());
        doc.put("nascimento", DateUtils.parseDate(createPessoaRequest.getNascimento()));
        doc.put("stack", stack);
        doc.put("busca",
                String.format("%s %s %s",
                        createPessoaRequest.getNome(),
                        createPessoaRequest.getApelido(),
                        createPessoaRequest.getStack() == null ? "" : String.join(" ", createPessoaRequest.getStack())
                ).trim()
        );

        final var collection = db.getCollection("pessoas");

        try {
            collection.insertOne(doc);
        } catch (final Exception e) {
            return null;
        }

        return id;
    }

    private PessoaResponse parse(final Document doc) {
        final var response = new PessoaResponse();
        response.setId(doc.getString("_id"));
        response.setNome(doc.getString("nome"));
        response.setApelido(doc.getString("apelido"));
        response.setNascimento(new java.sql.Date(doc.getDate("nascimento").getTime()).toLocalDate());
        response.setStack(doc.getList("stack", String.class));
        return response;
    }

    @Override
    public Optional<PessoaResponse> findById(final String id) {

        final var collection = db.getCollection("pessoas");
        final var doc = collection.find(new Document("_id", id)).first();

        return Optional.ofNullable(doc).map(this::parse);
    }

    @Override
    public List<PessoaResponse> findByFilter(final String textoBusca) {

        final var filter = Filters.text(textoBusca, new TextSearchOptions().caseSensitive(false));
        final var collection = db.getCollection("pessoas").withReadConcern(ReadConcern.LOCAL);

        final var result = collection.find(filter).limit(50);
        return StreamSupport.stream(result.spliterator(), true)
                .map(this::parse)
                .toList();
    }

    @Override
    public Long count() {
        final var collection = db.getCollection("pessoas");
        return collection.countDocuments();
    }
}
