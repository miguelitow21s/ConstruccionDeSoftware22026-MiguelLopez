package com.bank.application.usecases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioTransferencia;
import com.bank.domain.valueobjects.Dinero;

@Service
public class TransferirDineroUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final ServicioTransferencia servicioTransferencia;
    private final BitacoraRepositoryPort bitacoraRepository;
    private final AuthContextService authContextService;
    private final BigDecimal approvalThreshold;

    public TransferirDineroUseCase(CuentaRepositoryPort cuentaRepository,
                                   TransaccionRepositoryPort transaccionRepository,
                                   ServicioTransferencia servicioTransferencia,
                                   BitacoraRepositoryPort bitacoraRepository,
                                   AuthContextService authContextService,
                                   @Value("${bank.transfer.approval-threshold}") BigDecimal approvalThreshold) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.servicioTransferencia = servicioTransferencia;
        this.bitacoraRepository = bitacoraRepository;
        this.authContextService = authContextService;
        this.approvalThreshold = approvalThreshold;
    }

    @Transactional
    public Transaccion execute(String cuentaOrigenId, String cuentaDestinoId, BigDecimal monto, boolean esOperacionEmpresarial) {
        if (!authContextService.hasAnyRole("CLIENTE_NATURAL", "CLIENTE_EMPRESA", "EMPLEADO_EMPRESA")) {
            throw new SecurityException("No autorizado para crear transferencias");
        }

        var origen = cuentaRepository.findById(cuentaOrigenId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada"));
        var destino = cuentaRepository.findById(cuentaDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada"));

        String clienteRelacionado = authContextService.currentRelatedClientIdOrThrow();
        if (!clienteRelacionado.equals(origen.getClienteId())) {
            throw new SecurityException("No autorizado para operar la cuenta origen");
        }

        Dinero dinero = Dinero.positivo(monto);
        boolean requiereAprobacion = esOperacionEmpresarial && monto.compareTo(approvalThreshold) > 0;

        Transaccion transaccion = servicioTransferencia.transferir(origen, destino, dinero, requiereAprobacion);
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);
        Transaccion saved = transaccionRepository.save(transaccion);

        bitacoraRepository.save(new BitacoraEntry(
                UUID.randomUUID().toString(),
                "Transferencia_Creada",
                LocalDateTime.now(),
                usuarioActual(),
                rolActual(),
                saved.getId(),
                Map.of(
                        "cuentaOrigen", saved.getCuentaOrigen(),
                        "cuentaDestino", saved.getCuentaDestino(),
                        "monto", saved.getMonto().value(),
                        "estado", saved.getEstado().name(),
                        "requiereAprobacion", requiereAprobacion
                )
        ));
        return saved;
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
}
