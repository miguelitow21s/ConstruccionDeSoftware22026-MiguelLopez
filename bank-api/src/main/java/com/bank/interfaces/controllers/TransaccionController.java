package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.ListarTransaccionesUseCase;
import com.bank.interfaces.dtos.TransaccionResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/transacciones")
@Tag(name = "Transacciones", description = "Consulta del historial de transacciones")
@SecurityRequirement(name = "basicAuth")
public class TransaccionController {

    private final ListarTransaccionesUseCase listarTransaccionesUseCase;

    public TransaccionController(ListarTransaccionesUseCase listarTransaccionesUseCase) {
        this.listarTransaccionesUseCase = listarTransaccionesUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALISTA','VENTANILLA','COMERCIAL','SUPERVISOR_EMPRESA','EMPLEADO_EMPRESA','CLIENTE_NATURAL','CLIENTE_EMPRESA')")
    public List<TransaccionResponse> listar() {
        return listarTransaccionesUseCase.execute().stream()
                .map(tx -> new TransaccionResponse(
                        tx.getId(),
                        tx.getTipoTransaccion(),
                        tx.getMonto().value(),
                        tx.getFecha(),
                        tx.getCuentaOrigen(),
                        tx.getCuentaDestino(),
                        tx.getEstado()))
                .toList();
    }
}
