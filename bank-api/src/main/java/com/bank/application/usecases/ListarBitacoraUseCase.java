package com.bank.application.usecases;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import com.bank.application.services.AuthContextService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarBitacoraUseCase {

    private final BitacoraRepositoryPort bitacoraRepository;
    private final AuthContextService authContextService;

    public ListarBitacoraUseCase(BitacoraRepositoryPort bitacoraRepository,
                                 AuthContextService authContextService) {
        this.bitacoraRepository = bitacoraRepository;
        this.authContextService = authContextService;
    }

    public List<BitacoraEntry> execute(String idUsuario) {
        if (!authContextService.hasRole("ANALISTA")) {
            return bitacoraRepository.findByIdUsuario(authContextService.currentUserId());
        }

        if (idUsuario == null || idUsuario.isBlank()) {
            return bitacoraRepository.findAll();
        }
        return bitacoraRepository.findByIdUsuario(idUsuario);
    }
}
