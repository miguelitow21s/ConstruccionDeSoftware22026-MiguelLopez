package com.bank.application.usecases;

import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.EstadoTransaccion;
import com.bank.domain.services.ServicioTransferencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AprobarTransferenciaUseCase {

    private final TransaccionRepositoryPort transaccionRepository;
    private final CuentaRepositoryPort cuentaRepository;
    private final ServicioTransferencia servicioTransferencia;

    public AprobarTransferenciaUseCase(TransaccionRepositoryPort transaccionRepository,
                                       CuentaRepositoryPort cuentaRepository,
                                       ServicioTransferencia servicioTransferencia) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.servicioTransferencia = servicioTransferencia;
    }

    @Transactional
    public void aprobar(String transaccionId) {
        var transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaccion no encontrada"));

        if (transaccion.getEstado() != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("La transaccion no esta pendiente de aprobacion");
        }

        var origen = cuentaRepository.findByNumeroCuenta(transaccion.getCuentaOrigen())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada"));
        var destino = cuentaRepository.findByNumeroCuenta(transaccion.getCuentaDestino())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada"));

        servicioTransferencia.ejecutarTransferenciaPendiente(transaccion, origen, destino);
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);
        transaccionRepository.save(transaccion);
    }

    @Transactional
    public void rechazar(String transaccionId) {
        var transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaccion no encontrada"));

        if (transaccion.getEstado() != EstadoTransaccion.EN_ESPERA_APROBACION) {
            throw new IllegalStateException("La transaccion no esta pendiente de aprobacion");
        }

        transaccion.rechazar();
        transaccionRepository.save(transaccion);
    }
}
