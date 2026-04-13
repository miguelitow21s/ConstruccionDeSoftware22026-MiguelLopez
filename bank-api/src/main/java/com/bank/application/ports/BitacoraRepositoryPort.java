package com.bank.application.ports;

import java.util.List;

public interface BitacoraRepositoryPort {

    void save(BitacoraEntry entry);

    List<BitacoraEntry> findAll();

    List<BitacoraEntry> findByIdUsuario(String idUsuario);

    List<BitacoraEntry> findByIdProductoAfectadoIn(List<String> idsProductoAfectado);
}
