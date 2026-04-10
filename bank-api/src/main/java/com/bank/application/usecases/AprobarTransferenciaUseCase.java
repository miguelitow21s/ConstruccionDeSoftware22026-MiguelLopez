package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.services.ServicioTransferencia;

@Service
public class AprobarTransferenciaUseCase {

    private final TransaccionRepositoryPort transaccionRepository;
    private final CuentaRepositoryPort cuentaRepository;
    private final ServicioTransferencia servicioTransferencia;
    private final BitacoraRepositoryPort bitacoraRepository;
    private final AuthContextService authContextService;

    public AprobarTransferenciaUseCase(TransaccionRepositoryPort transaccionRepository,
                                       CuentaRepositoryPort cuentaRepository,
                                       ServicioTransferencia servicioTransferencia,
                                       BitacoraRepositoryPort bitacoraRepository,
                                       AuthContextService authContextService) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.servicioTransferencia = servicioTransferencia;
        this.bitacoraRepository = bitacoraRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public void aprobar(String transaccionId) {
        validarRolAprobador();

        var transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaccion no encontrada"));

        if (transaccion.getEstado() != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("La transaccion no esta pendiente de aprobacion");
        }

        var origen = cuentaRepository.findByNumeroCuenta(transaccion.getCuentaOrigen())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada"));
        var destino = cuentaRepository.findByNumeroCuenta(transaccion.getCuentaDestino())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada"));

        validarEmpresaSupervisor(origen.getClienteId());

        servicioTransferencia.ejecutarTransferenciaPendiente(transaccion, origen, destino);
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);
        transaccionRepository.save(transaccion);

        bitacoraRepository.save(new BitacoraEntry(
            UUID.randomUUID().toString(),
            "Transferencia_Aprobada",
            LocalDateTime.now(),
            usuarioActual(),
            rolActual(),
            transaccion.getId(),
            Map.of(
                "estadoFinal", transaccion.getEstado().name(),
                "cuentaOrigen", transaccion.getCuentaOrigen(),
                "cuentaDestino", transaccion.getCuentaDestino(),
                "monto", transaccion.getMonto().value()
            )
        ));
    }

    @Transactional
    public void rechazar(String transaccionId) {
        validarRolAprobador();

        var transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaccion no encontrada"));

        if (transaccion.getEstado() != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("La transaccion no esta pendiente de aprobacion");
        }

        var origen = cuentaRepository.findByNumeroCuenta(transaccion.getCuentaOrigen())
            .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada"));
        validarEmpresaSupervisor(origen.getClienteId());

        transaccion.rechazar();
        transaccionRepository.save(transaccion);

        bitacoraRepository.save(new BitacoraEntry(
                UUID.randomUUID().toString(),
                "Transferencia_Rechazada",
                LocalDateTime.now(),
                usuarioActual(),
                rolActual(),
                transaccion.getId(),
                Map.of(
                        "estadoFinal", transaccion.getEstado().name(),
                        "motivo", "rechazada por aprobador"
                )
        ));
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private String rolActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "SYSTEM";
        }
        return auth.getAuthorities().iterator().next().getAuthority();
    }

    private void validarRolAprobador() {
        if (!authContextService.hasRole("SUPERVISOR_EMPRESA")) {
            throw new SecurityException("Solo Supervisor de Empresa puede aprobar o rechazar transferencias");
        }
    }

    private void validarEmpresaSupervisor(String clienteEmpresaOrigen) {
        String empresaSupervisor = authContextService.currentRelatedClientIdOrThrow();
        if (!empresaSupervisor.equals(clienteEmpresaOrigen)) {
            throw new SecurityException("No autorizado para aprobar o rechazar operaciones de otra empresa");
        }
    }
}
