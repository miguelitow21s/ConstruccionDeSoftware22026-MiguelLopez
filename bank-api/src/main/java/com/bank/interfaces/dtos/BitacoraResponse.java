package com.bank.interfaces.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record BitacoraResponse(
        String idBitacora,
        String tipoOperacion,
        LocalDateTime fechaHoraOperacion,
        String idUsuario,
        String rolUsuario,
        String idProductoAfectado,
        Map<String, Object> datosDetalle
) {
}
