package com.bank.application.usecases;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.ports.CuentaRepositoryPort;
import com.bank.application.ports.PrestamoRepositoryPort;
import com.bank.application.services.AuthContextService;
import com.bank.domain.entities.Cuenta;
import com.bank.domain.entities.Prestamo;
import com.bank.domain.valueobjects.Dinero;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class DesembolsarPrestamoUseCase {

    private final PrestamoRepositoryPort prestamoRepository;
    private final CuentaRepositoryPort cuentaRepository;
    private final BitacoraRepositoryPort bitacoraRepository;
    private final AuthContextService authContextService;

    public DesembolsarPrestamoUseCase(PrestamoRepositoryPort prestamoRepository,
                                      CuentaRepositoryPort cuentaRepository,
                                      BitacoraRepositoryPort bitacoraRepository,
                                      AuthContextService authContextService) {
        this.prestamoRepository = prestamoRepository;
        this.cuentaRepository = cuentaRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public Prestamo execute(String prestamoId, String numeroCuentaDestino) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new IllegalArgumentException("Prestamo no encontrado"));

        Cuenta cuentaDestino = cuentaRepository.findByNumeroCuenta(numeroCuentaDestino)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada"));

        if (!cuentaDestino.getClienteId().equals(prestamo.getClienteSolicitanteId())) {
            throw new IllegalStateException("La cuenta destino no pertenece al cliente del prestamo");
        }

        cuentaDestino.validarCuentaOperativa();
        prestamo.desembolsar(numeroCuentaDestino);

        cuentaDestino.depositar(Dinero.positivo(prestamo.getMontoAprobado()));
        cuentaRepository.save(cuentaDestino);
        Prestamo saved = prestamoRepository.save(prestamo);

        bitacoraRepository.save(new BitacoraEntry(
                UUID.randomUUID().toString(),
                "Desembolso_Prestamo",
                LocalDateTime.now(),
                authContextService.currentUserId(),
                authContextService.currentRole(),
                saved.getId(),
                Map.of(
                        "numeroCuentaDestino", numeroCuentaDestino,
                        "montoDesembolsado", saved.getMontoAprobado(),
                        "nuevoEstado", saved.getEstado().name()
                )
        ));

        return saved;
    }
}
