package com.bank.application.usecases;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.TransaccionRepositoryPort;
import com.bank.domain.entities.EstadoTransaccion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VencerTransferenciasPendientesUseCase {

    private final TransaccionRepositoryPort transaccionRepository;
    private final BitacoraRepositoryPort bitacoraRepository;
    private final long expirationMinutes;

    public VencerTransferenciasPendientesUseCase(TransaccionRepositoryPort transaccionRepository,
                                                 BitacoraRepositoryPort bitacoraRepository,
                                                 @Value("${bank.transfer.approval-expiration-minutes}") long expirationMinutes) {
        this.transaccionRepository = transaccionRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.expirationMinutes = expirationMinutes;
    }

    // Revisa cada minuto transferencias pendientes que superaron la ventana de aprobacion.
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void execute() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expirationMinutes);
        List<com.bank.domain.entities.Transaccion> expiring = transaccionRepository
                .findByEstadoAndFechaBefore(EstadoTransaccion.EN_ESPERA_APROBACION, cutoff);

        expiring.forEach(transaccion -> {
            transaccion.vencer();
            transaccionRepository.save(transaccion);
            bitacoraRepository.save(new BitacoraEntry(
                    UUID.randomUUID().toString(),
                    "Transferencia_Vencida",
                    LocalDateTime.now(),
                    "system",
                    "SYSTEM",
                    transaccion.getId(),
                    Map.of(
                            "motivo", "vencida por falta de aprobacion en el tiempo establecido",
                            "estadoFinal", transaccion.getEstado().name()
                    )
            ));
        });
    }
}
