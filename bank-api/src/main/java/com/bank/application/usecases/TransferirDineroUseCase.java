package com.bank.application.usecases;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.Transaccion;
import com.bank.domain.services.ServicioTransferencia;
import com.bank.domain.valueobjects.Dinero;

@Service
public class TransferirDineroUseCase {

    private final CuentaRepositoryPort cuentaRepository;
    private final TransaccionRepositoryPort transaccionRepository;
    private final ServicioTransferencia servicioTransferencia;
    private final BigDecimal approvalThreshold;

    public TransferirDineroUseCase(CuentaRepositoryPort cuentaRepository,
                                   TransaccionRepositoryPort transaccionRepository,
                                   ServicioTransferencia servicioTransferencia,
                                   @Value("${bank.transfer.approval-threshold}") BigDecimal approvalThreshold) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.servicioTransferencia = servicioTransferencia;
        this.approvalThreshold = approvalThreshold;
    }

    @Transactional
    public Transaccion execute(String cuentaOrigenId, String cuentaDestinoId, BigDecimal monto, boolean esOperacionEmpresarial) {
        var origen = cuentaRepository.findById(cuentaOrigenId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada"));
        var destino = cuentaRepository.findById(cuentaDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada"));

        Dinero dinero = Dinero.positivo(monto);
        boolean requiereAprobacion = esOperacionEmpresarial && monto.compareTo(approvalThreshold) > 0;

        Transaccion transaccion = servicioTransferencia.transferir(origen, destino, dinero, requiereAprobacion);
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);
        return transaccionRepository.save(transaccion);
    }
}
