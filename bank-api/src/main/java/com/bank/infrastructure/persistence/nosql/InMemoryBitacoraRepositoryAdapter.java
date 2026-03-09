package com.bank.infrastructure.persistence.nosql;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    public List<BitacoraDocument> findAll() {
        return List.copyOf(store);
    }
}
