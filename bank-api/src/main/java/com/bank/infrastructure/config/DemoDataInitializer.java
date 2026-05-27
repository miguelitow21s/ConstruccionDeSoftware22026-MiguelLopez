package com.bank.infrastructure.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.bank.application.ports.AccountRepositoryPort;
import com.bank.application.ports.ClientRepositoryPort;
import com.bank.application.ports.SystemUserRepositoryPort;
import com.bank.domain.entities.Account;
import com.bank.domain.entities.AccountStatus;
import com.bank.domain.entities.AccountType;
import com.bank.domain.entities.Client;
import com.bank.domain.entities.ClientType;
import com.bank.domain.entities.SystemRole;
import com.bank.domain.entities.SystemUser;
import com.bank.domain.entities.UserStatus;
import com.bank.domain.valueobjects.AccountNumber;
import com.bank.domain.valueobjects.Email;
import com.bank.domain.valueobjects.Money;

// Carga datos de demo al inicio: representante legal, empresa cliente, su usuario de sistema y su cuenta bancaria.
// El representante debe existir antes que la empresa: CreateClientUseCase exige que sea un NATURAL_PERSON_CLIENT real.
@Component
@Order(2)
public class DemoDataInitializer implements CommandLineRunner {

    static final String LEGAL_REP_CLIENT_ID = "representante_demo_id";
    static final String LEGAL_REP_IDENTIFICATION = "12345678";

    static final String COMPANY_CLIENT_ID = "empresa_demo_id";
    static final String COMPANY_IDENTIFICATION = "900123456";
    static final String COMPANY_ACCOUNT_NUMBER = "10000001";

    private final ClientRepositoryPort clientRepository;
    private final SystemUserRepositoryPort systemUserRepository;
    private final AccountRepositoryPort accountRepository;

    public DemoDataInitializer(ClientRepositoryPort clientRepository,
                               SystemUserRepositoryPort systemUserRepository,
                               AccountRepositoryPort accountRepository) {
        this.clientRepository = clientRepository;
        this.systemUserRepository = systemUserRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        createLegalRepresentativeIfAbsent();
        createCompanyClientIfAbsent();
        createCompanySystemUserIfAbsent();
        createCompanyAccountIfAbsent();
    }

    // El representante legal es una persona natural que firma en nombre de la empresa.
    // Debe existir como NATURAL_PERSON_CLIENT antes de crear el cliente empresa.
    private void createLegalRepresentativeIfAbsent() {
        if (clientRepository.findById(LEGAL_REP_CLIENT_ID).isPresent()) return;

        clientRepository.save(new Client(
                LEGAL_REP_CLIENT_ID,
                LEGAL_REP_IDENTIFICATION,
                "Carlos Gomez",
                new Email("representante@techsolutions.com"),
                "3001234567",
                LocalDate.of(1985, 6, 15),
                "Carrera 7 # 32-10, Bogota",
                ClientType.NATURAL_PERSON_CLIENT,
                null
        ));
    }

    private void createCompanyClientIfAbsent() {
        if (clientRepository.findById(COMPANY_CLIENT_ID).isPresent()) return;

        clientRepository.save(new Client(
                COMPANY_CLIENT_ID,
                COMPANY_IDENTIFICATION,
                "Tech Solutions Corp",
                new Email("empresa@techsolutions.com"),
                "6011234567",
                null,
                "Calle 100 # 15-20, Bogota",
                ClientType.BUSINESS_CLIENT,
                LEGAL_REP_CLIENT_ID
        ));
    }

    private void createCompanySystemUserIfAbsent() {
        if (systemUserRepository.findByIdIdentification(COMPANY_IDENTIFICATION).isPresent()) return;

        // BUSINESS_CLIENT role does not require birthDate (not a natural person)
        systemUserRepository.save(new SystemUser(
                100L,
                COMPANY_CLIENT_ID,
                "Tech Solutions Corp",
                COMPANY_IDENTIFICATION,
                new Email("empresa@techsolutions.com"),
                "6011234567",
                null,
                "Calle 100 # 15-20, Bogota",
                SystemRole.BUSINESS_CLIENT,
                UserStatus.ACTIVE
        ));
    }

    private void createCompanyAccountIfAbsent() {
        if (accountRepository.findByAccountNumber(COMPANY_ACCOUNT_NUMBER).isPresent()) return;

        accountRepository.save(new Account(
                "empresa_account_001",
                new AccountNumber(COMPANY_ACCOUNT_NUMBER),
                Money.zero(),
                AccountType.BUSINESS,
                COMPANY_CLIENT_ID,
                COMPANY_IDENTIFICATION,
                "COP",
                LocalDate.now(),
                AccountStatus.ACTIVE
        ));
    }
}
