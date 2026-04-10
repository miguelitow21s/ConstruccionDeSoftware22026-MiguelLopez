package com.bank.application.usecases;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Transaccion;

@Service
public class CrearPagosMasivosUseCase {

    private final TransferirDineroUseCase transferirDineroUseCase;
    private final AuthContextService authContextService;

    public CrearPagosMasivosUseCase(TransferirDineroUseCase transferirDineroUseCase,
                                    AuthContextService authContextService) {
        this.transferirDineroUseCase = transferirDineroUseCase;
        this.authContextService = authContextService;
    }

    public List<Transaccion> execute(String cuentaOrigenId, List<PagoMasivoItem> pagos) {
        if (!authContextService.hasRole("EMPLEADO_EMPRESA")) {
            throw new SecurityException("No autorizado para crear pagos masivos");
        }
        if (pagos == null || pagos.isEmpty()) {
            throw new IllegalArgumentException("La lista de pagos masivos no puede estar vacia");
        }

        return pagos.stream()
                .map(pago -> transferirDineroUseCase.execute(
                        cuentaOrigenId,
                        pago.cuentaDestinoId(),
                        pago.monto(),
                        true
                ))
                .toList();
    }

    public record PagoMasivoItem(String cuentaDestinoId, BigDecimal monto) {
    }
}
