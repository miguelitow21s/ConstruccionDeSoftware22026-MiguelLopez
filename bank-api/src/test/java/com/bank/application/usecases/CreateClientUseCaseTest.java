package com.bank.application.usecases;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.bank.application.ports.ClientRepositoryPort;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.ClientType;
import com.bank.domain.valueobjects.Email;

class CreateClientUseCaseTest {

    @Test
    void debeCreateClientCuandoNoExisteEmailNiIdentification() {
        FakeClientRepository repo = new FakeClientRepository();
        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        Client creado = useCase.execute("10101010", "Miguel Lopez", "miguel@bank.com", "3001234567", LocalDate.of(1990, 1, 1), "Street 123", null, null);

        assertEquals("10101010", creado.getIdIdentification());
        assertEquals(1, repo.storage.size());
    }

    @Test
    void debeFallarSiIdentificationYaExiste() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));

        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("10101010", "Client Dos", "dos@bank.com", "3002222222", LocalDate.of(1990, 1, 1), "Street 123", null, null));
        assertEquals("A client with that identification already exists", thrown.getMessage());
    }

    @Test
    void debeFallarSiEmailYaExiste() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("id-1", "10101010", "Client Uno", new Email("uno@bank.com"), "3001111111"));

        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("20202020", "Client Dos", "uno@bank.com", "3002222222", LocalDate.of(1990, 1, 1), "Street 123", null, null));
        assertEquals("A client with that email already exists", thrown.getMessage());
    }

    @Test
    void debeCreateClientCompanyCuandoRepresentanteEsPersonaNatural() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("rep-1", "11111111", "Representante", new Email("rep@bank.com"), "3001111111", ClientType.NATURAL_PERSON_CLIENT, null));

        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        Client company = useCase.execute("900999888", "Company S.A.S", "company@bank.com", "6011234567", LocalDate.of(1990, 1, 1), "Avenue 456", "BUSINESS_CLIENT", "rep-1");

        assertEquals(ClientType.BUSINESS_CLIENT, company.getClientType());
        assertEquals("rep-1", company.getLegalRepresentativeId());
    }

    @Test
    void debeFallarClientCompanySinRepresentanteLegal() {
        FakeClientRepository repo = new FakeClientRepository();
        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("900999888", "Company S.A.S", "company@bank.com", "6011234567", LocalDate.of(1990, 1, 1), "Avenue 456", "BUSINESS_CLIENT", null)
        );
        assertEquals("Legal representative is required for business clients", thrown.getMessage());
    }

    @Test
    void debeFallarClientCompanySiRepresentanteNoEsPersonaNatural() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("rep-2", "900111222", "Otra Company", new Email("otra@bank.com"), "6019999999", ClientType.BUSINESS_CLIENT, "rep-x"));

        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("900999888", "Company S.A.S", "company@bank.com", "6011234567", LocalDate.of(1990, 1, 1), "Avenue 456", "BUSINESS_CLIENT", "rep-2")
        );
        assertEquals("The legal representative must be a natural person client", thrown.getMessage());
    }

    @Test
    void debeCreateClientCompanySinBirthDate() {
        FakeClientRepository repo = new FakeClientRepository();
        repo.storage.add(new Client("rep-1", "11111111", "Representante", new Email("rep@bank.com"), "3001111111", ClientType.NATURAL_PERSON_CLIENT, null));

        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        Client company = useCase.execute("900888777", "Company Two", "company2@bank.com", "6012233445", null, "Avenue 123", "BUSINESS_CLIENT", "rep-1");

        assertEquals(ClientType.BUSINESS_CLIENT, company.getClientType());
        assertEquals("rep-1", company.getLegalRepresentativeId());
        assertEquals(null, company.getBirthDate());
    }

    @Test
    void debeFallarSiClienteNaturalEsMenorDeEdad() {
        FakeClientRepository repo = new FakeClientRepository();
        CreateClientUseCase useCase = new CreateClientUseCase(repo);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute("30303030", "Client Joven", "joven@bank.com", "3007777777", LocalDate.now().minusYears(17), "Street 777", null, null)
        );
        assertEquals("Client must be an adult", thrown.getMessage());
    }

    private static final class FakeClientRepository implements ClientRepositoryPort {
        private final List<Client> storage = new ArrayList<>();

        @Override
        public Client save(Client client) {
            storage.removeIf(existing -> existing.getId().equals(client.getId()));
            storage.add(client);
            return client;
        }

        @Override
        public Optional<Client> findById(String id) {
            return storage.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Client> findByEmail(String email) {
            return storage.stream().filter(c -> c.getEmail().value().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<Client> findByIdIdentification(String identificationId) {
            return storage.stream().filter(c -> c.getIdIdentification().equals(identificationId)).findFirst();
        }
    }
}
