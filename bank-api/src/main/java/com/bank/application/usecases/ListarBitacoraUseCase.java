package com.bank.application.usecases;

import com.bank.application.ports.BitacoraEntry;
import com.bank.application.ports.BitacoraRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarBitacoraUseCase {

    private final BitacoraRepositoryPort bitacoraRepository;

    public ListarBitacoraUseCase(BitacoraRepositoryPort bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    public List<BitacoraEntry> execute(String idUsuario) {
        if (idUsuario == null || idUsuario.isBlank()) {
            return bitacoraRepository.findAll();
        }
        return bitacoraRepository.findByIdUsuario(idUsuario);
    }
}
