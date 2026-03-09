package com.bank.application.ports;

import java.time.LocalDateTime;
import java.util.Map;

public record BitacoraEntry(
        String idBitacora,
        String tipoOperacion,
        LocalDateTime fechaHoraOperacion,
        String idUsuario,
        String rolUsuario,
        String idProductoAfectado,
        Map<String, Object> datosDetalle
) {
}
