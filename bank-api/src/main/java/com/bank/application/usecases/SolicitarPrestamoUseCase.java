package com.bank.application.usecases;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Prestamo;
import com.bank.domain.entities.TipoPrestamo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class SolicitarPrestamoUseCase {

    private final PrestamoRepositoryPort prestamoRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final BitacoraRepositoryPort bitacoraRepository;
    private final AuthContextService authContextService;

    public SolicitarPrestamoUseCase(PrestamoRepositoryPort prestamoRepository,
                                    ClienteRepositoryPort clienteRepository,
                                    BitacoraRepositoryPort bitacoraRepository,
                                    AuthContextService authContextService) {
        this.prestamoRepository = prestamoRepository;
        this.clienteRepository = clienteRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public Prestamo execute(TipoPrestamo tipoPrestamo,
                            String clienteSolicitanteId,
                            BigDecimal montoSolicitado,
                            BigDecimal tasaInteres,
                            int plazoMeses) {
        clienteRepository.findById(clienteSolicitanteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente solicitante no encontrado"));

        Prestamo prestamo = new Prestamo(
                tipoPrestamo,
                clienteSolicitanteId,
                montoSolicitado,
                tasaInteres,
                plazoMeses
        );

        Prestamo saved = prestamoRepository.save(prestamo);

        bitacoraRepository.save(new BitacoraEntry(
                UUID.randomUUID().toString(),
                "Solicitud_Prestamo",
                LocalDateTime.now(),
                authContextService.currentUserId(),
                authContextService.currentRole(),
                saved.getId(),
                Map.of(
                        "estado", saved.getEstado().name(),
                        "montoSolicitado", saved.getMontoSolicitado(),
                        "tipoPrestamo", saved.getTipoPrestamo().name()
                )
        ));

        return saved;
    }
}
