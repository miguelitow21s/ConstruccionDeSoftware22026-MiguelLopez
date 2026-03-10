package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.ListarBitacoraUseCase;
import com.bank.interfaces.dtos.BitacoraResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/bitacora")
@Tag(name = "Bitácora", description = "Consulta de registros de auditoría y trazabilidad")
@SecurityRequirement(name = "basicAuth")
public class BitacoraController {

    private final ListarBitacoraUseCase listarBitacoraUseCase;

    public BitacoraController(ListarBitacoraUseCase listarBitacoraUseCase) {
        this.listarBitacoraUseCase = listarBitacoraUseCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar bitácora de operaciones", 
               description = "Lista los registros de auditoría. Los analistas pueden filtrar por usuario, " +
                           "otros roles solo ven sus propias operaciones.")
    public List<BitacoraResponse> listar(@RequestParam(required = false) String idUsuario) {
        return listarBitacoraUseCase.execute(idUsuario).stream()
                .map(entry -> new BitacoraResponse(
                        entry.idBitacora(),
                        entry.tipoOperacion(),
                        entry.fechaHoraOperacion(),
                        entry.idUsuario(),
                        entry.rolUsuario(),
                        entry.idProductoAfectado(),
                        entry.datosDetalle()
                ))
                .toList();
    }
}
