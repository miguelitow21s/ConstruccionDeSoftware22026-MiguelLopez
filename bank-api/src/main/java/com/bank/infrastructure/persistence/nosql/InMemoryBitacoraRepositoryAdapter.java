package com.bank.infrastructure.persistence.nosql;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InMemoryBitacoraRepositoryAdapter implements BitacoraRepositoryPort {

    private final List<BitacoraDocument> store = new ArrayList<>();

    @Override
    public void save(BitacoraEntry entry) {
        BitacoraDocument doc = new BitacoraDocument();
        doc.setIdBitacora(entry.idBitacora());
        doc.setTipoOperacion(entry.tipoOperacion());
        doc.setFechaHoraOperacion(entry.fechaHoraOperacion());
        doc.setIdUsuario(entry.idUsuario());
        doc.setRolUsuario(entry.rolUsuario());
        doc.setIdProductoAfectado(entry.idProductoAfectado());
        doc.setDatosDetalle(entry.datosDetalle());
        store.add(doc);
    }

    @Override
    public List<BitacoraEntry> findAll() {
        return store.stream().map(this::toEntry).collect(Collectors.toList());
    }

    @Override
    public List<BitacoraEntry> findByIdUsuario(String idUsuario) {
        return store.stream()
                .filter(doc -> doc.getIdUsuario() != null && doc.getIdUsuario().equals(idUsuario))
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    private BitacoraEntry toEntry(BitacoraDocument doc) {
        return new BitacoraEntry(
                doc.getIdBitacora(),
                doc.getTipoOperacion(),
                doc.getFechaHoraOperacion(),
                doc.getIdUsuario(),
                doc.getRolUsuario(),
                doc.getIdProductoAfectado(),
                doc.getDatosDetalle()
        );
    }
}
