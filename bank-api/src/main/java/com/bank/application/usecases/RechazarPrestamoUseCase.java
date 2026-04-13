package com.bank.application.usecases;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Prestamo;

@Service
public class RechazarPrestamoUseCase {

    private final PrestamoRepositoryPort prestamoRepository;
    private final BitacoraRepositoryPort bitacoraRepository;
    private final AuthContextService authContextService;

    public RechazarPrestamoUseCase(PrestamoRepositoryPort prestamoRepository,
                                   BitacoraRepositoryPort bitacoraRepository,
                                   AuthContextService authContextService) {
        this.prestamoRepository = prestamoRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public Prestamo execute(String prestamoId) {
                if (!authContextService.hasRole("ANALISTA")) {
                        throw new SecurityException("No autorizado para rechazar prestamos");
                }

        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new IllegalArgumentException("Prestamo no encontrado"));

        prestamo.rechazar();
        Prestamo saved = prestamoRepository.save(prestamo);

        bitacoraRepository.save(new BitacoraEntry(
                UUID.randomUUID().toString(),
                "Rechazo_Prestamo",
                LocalDateTime.now(),
                authContextService.currentUserId(),
                authContextService.currentRole(),
                saved.getId(),
                Map.of(
                        "idUsuarioRechazo", authContextService.currentUserId(),
                        "fechaRechazo", LocalDateTime.now().toString(),
                        "estadoAnterior", "EN_ESTUDIO",
                        "nuevoEstado", saved.getEstado().name()
                )
        ));

        return saved;
    }
}
