package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.domain.entities.Cliente;
import com.bank.domain.entities.TipoCliente;
import com.bank.domain.valueobjects.Email;

@Service
public class CrearClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;

    public CrearClienteUseCase(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente execute(String idIdentificacion,
                           String nombre,
                           String email,
                           String telefono,
                           String tipoClienteRaw,
                           String representanteLegalId) {
        clienteRepository.findByIdIdentificacion(idIdentificacion).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un cliente con esa identificacion");
        });
        clienteRepository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un cliente con ese email");
        });

        TipoCliente tipoCliente = (tipoClienteRaw == null || tipoClienteRaw.isBlank())
                ? TipoCliente.CLIENTE_PERSONA_NATURAL
                : TipoCliente.valueOf(tipoClienteRaw);

        if (tipoCliente == TipoCliente.CLIENTE_EMPRESA) {
            if (representanteLegalId == null || representanteLegalId.isBlank()) {
                throw new IllegalArgumentException("Representante legal obligatorio para cliente empresa");
            }

            Cliente representante = clienteRepository.findById(representanteLegalId)
                    .orElseThrow(() -> new IllegalArgumentException("Representante legal no encontrado"));

            if (representante.getTipoCliente() != TipoCliente.CLIENTE_PERSONA_NATURAL) {
                throw new IllegalArgumentException("El representante legal debe ser un cliente persona natural");
            }
        }

        Cliente cliente = new Cliente(idIdentificacion, nombre, new Email(email), telefono, tipoCliente, representanteLegalId);
        return clienteRepository.save(cliente);
    }
}
