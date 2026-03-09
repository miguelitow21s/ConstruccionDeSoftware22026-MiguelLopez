package com.bank.infrastructure.persistence.nosql;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Primary
@ConditionalOnProperty(prefix = "bank.bitacora", name = "storage", havingValue = "mongodb")
public class MongoBitacoraRepositoryAdapter implements BitacoraRepositoryPort {

    private final MongoBitacoraSpringRepository repository;

    public MongoBitacoraRepositoryAdapter(MongoBitacoraSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    @SuppressWarnings("null")
    public void save(BitacoraEntry entry) {
        repository.save(Objects.requireNonNull(toDocument(entry)));
    }

    @Override
    public List<BitacoraEntry> findAll() {
        return repository.findAll().stream().map(this::toEntry).toList();
    }

    @Override
    public List<BitacoraEntry> findByIdUsuario(String idUsuario) {
        return repository.findByIdUsuario(idUsuario).stream().map(this::toEntry).toList();
    }

    private BitacoraDocument toDocument(BitacoraEntry entry) {
        BitacoraDocument doc = new BitacoraDocument();
        doc.setIdBitacora(entry.idBitacora());
        doc.setTipoOperacion(entry.tipoOperacion());
        doc.setFechaHoraOperacion(entry.fechaHoraOperacion());
        doc.setIdUsuario(entry.idUsuario());
        doc.setRolUsuario(entry.rolUsuario());
        doc.setIdProductoAfectado(entry.idProductoAfectado());
        doc.setDatosDetalle(entry.datosDetalle());
        return doc;
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
