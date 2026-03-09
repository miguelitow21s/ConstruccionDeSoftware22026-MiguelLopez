package com.bank.interfaces.controllers;

import com.bank.application.usecases.ListarBitacoraUseCase;
import com.bank.interfaces.dtos.BitacoraResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bitacora")
public class BitacoraController {

    private final ListarBitacoraUseCase listarBitacoraUseCase;

    public BitacoraController(ListarBitacoraUseCase listarBitacoraUseCase) {
        this.listarBitacoraUseCase = listarBitacoraUseCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
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
