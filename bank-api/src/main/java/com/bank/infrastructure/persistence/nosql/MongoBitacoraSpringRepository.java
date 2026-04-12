package com.bank.infrastructure.persistence.nosql;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoBitacoraSpringRepository extends MongoRepository<BitacoraDocument, String> {

    List<BitacoraDocument> findByIdUsuario(String idUsuario);

    List<BitacoraDocument> findByIdProductoAfectadoIn(List<String> idsProductoAfectado);
}
