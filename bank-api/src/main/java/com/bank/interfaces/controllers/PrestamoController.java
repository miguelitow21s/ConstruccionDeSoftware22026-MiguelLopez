package com.bank.interfaces.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bank.application.usecases.AprobarPrestamoUseCase;
import com.bank.application.usecases.DesembolsarPrestamoUseCase;
import com.bank.application.usecases.ListarPrestamosUseCase;
import com.bank.application.usecases.RechazarPrestamoUseCase;
import com.bank.application.usecases.SolicitarPrestamoUseCase;
import com.bank.domain.entities.Prestamo;
import com.bank.interfaces.dtos.AprobarPrestamoRequest;
import com.bank.interfaces.dtos.DesembolsarPrestamoRequest;
import com.bank.interfaces.dtos.PrestamoResponse;
import com.bank.interfaces.dtos.SolicitarPrestamoRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/prestamos")
@Tag(name = "Préstamos", description = "Gestión de préstamos y créditos bancarios")
@SecurityRequirement(name = "basicAuth")
public class PrestamoController {

    private final SolicitarPrestamoUseCase solicitarPrestamoUseCase;
    private final AprobarPrestamoUseCase aprobarPrestamoUseCase;
    private final RechazarPrestamoUseCase rechazarPrestamoUseCase;
    private final DesembolsarPrestamoUseCase desembolsarPrestamoUseCase;
    private final ListarPrestamosUseCase listarPrestamosUseCase;

    public PrestamoController(SolicitarPrestamoUseCase solicitarPrestamoUseCase,
                              AprobarPrestamoUseCase aprobarPrestamoUseCase,
                              RechazarPrestamoUseCase rechazarPrestamoUseCase,
                              DesembolsarPrestamoUseCase desembolsarPrestamoUseCase,
                              ListarPrestamosUseCase listarPrestamosUseCase) {
        this.solicitarPrestamoUseCase = solicitarPrestamoUseCase;
        this.aprobarPrestamoUseCase = aprobarPrestamoUseCase;
        this.rechazarPrestamoUseCase = rechazarPrestamoUseCase;
        this.desembolsarPrestamoUseCase = desembolsarPrestamoUseCase;
        this.listarPrestamosUseCase = listarPrestamosUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLIENTE_NATURAL','CLIENTE_EMPRESA','COMERCIAL')")
    @Operation(summary = "Solicitar un préstamo", 
               description = "Permite a clientes y empleados comerciales crear una nueva solicitud de préstamo. " +
                           "El préstamo inicia en estado 'En estudio'.")
    public PrestamoResponse solicitar(@Valid @RequestBody SolicitarPrestamoRequest request) {
        Prestamo prestamo = solicitarPrestamoUseCase.execute(
                request.tipoPrestamo(),
                request.clienteSolicitanteId(),
                request.montoSolicitado(),
                request.tasaInteres(),
                request.plazoMeses()
        );
        return toResponse(prestamo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALISTA','COMERCIAL','SUPERVISOR_EMPRESA','EMPLEADO_EMPRESA','CLIENTE_NATURAL','CLIENTE_EMPRESA')")
    public List<PrestamoResponse> listar() {
        return listarPrestamosUseCase.execute().stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ANALISTA')")
    @Operation(summary = "Aprobar préstamo", 
               description = "Solo el Analista Interno puede aprobar préstamos. El estado cambia a 'Aprobado'.")
    public PrestamoResponse aprobar(@PathVariable String id, @Valid @RequestBody AprobarPrestamoRequest request) {
        return toResponse(aprobarPrestamoUseCase.execute(id, request.montoAprobado()));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ANALISTA')")
    public PrestamoResponse rechazar(@PathVariable String id) {
        return toResponse(rechazarPrestamoUseCase.execute(id));
    }

    @PostMapping("/{id}/desembolsar")
    @PreAuthorize("hasRole('ANALISTA')")
    @Operation(summary = "Desembolsar préstamo", 
               description = "Transfiere el monto aprobado a la cuenta destino. Solo disponible para préstamos aprobados.")
    public PrestamoResponse desembolsar(@PathVariable String id, @Valid @RequestBody DesembolsarPrestamoRequest request) {
        return toResponse(desembolsarPrestamoUseCase.execute(id, request.numeroCuentaDestino()));
    }

    private PrestamoResponse toResponse(Prestamo prestamo) {
        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getTipoPrestamo(),
                prestamo.getClienteSolicitanteId(),
                prestamo.getMontoSolicitado(),
                prestamo.getMontoAprobado(),
                prestamo.getTasaInteres(),
                prestamo.getPlazoMeses(),
                prestamo.getEstado(),
                prestamo.getFechaAprobacion(),
                prestamo.getFechaDesembolso(),
                prestamo.getCuentaDestinoDesembolso()
        );
    }
}
