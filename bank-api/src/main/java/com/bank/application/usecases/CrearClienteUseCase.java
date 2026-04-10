package com.bank.application.usecases;

import org.springframework.stereotype.Service;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.domain.entities.Cliente;
import com.bank.domain.valueobjects.Email;

@Service
public class CrearClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;

    public CrearClienteUseCase(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente execute(String idIdentificacion, String nombre, String email, String telefono) {
        clienteRepository.findByIdIdentificacion(idIdentificacion).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un cliente con esa identificacion");
        });
        clienteRepository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un cliente con ese email");
        });
        Cliente cliente = new Cliente(idIdentificacion, nombre, new Email(email), telefono);
        return clienteRepository.save(cliente);
    }
}
