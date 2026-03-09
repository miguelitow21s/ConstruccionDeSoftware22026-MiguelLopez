package com.bank.application.usecases;

import com.bank.application.ports.ClienteRepositoryPort;
import com.bank.domain.entities.Cliente;
import com.bank.domain.valueobjects.Email;
import org.springframework.stereotype.Service;

@Service
public class CrearClienteUseCase {

    private final ClienteRepositoryPort clienteRepository;

    public CrearClienteUseCase(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente execute(String nombre, String email, String telefono) {
        clienteRepository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un cliente con ese email");
        });
        Cliente cliente = new Cliente(nombre, new Email(email), telefono);
        return clienteRepository.save(cliente);
    }
}
