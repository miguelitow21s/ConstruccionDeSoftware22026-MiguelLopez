package com.bank.infrastructure.persistence.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.bank.infrastructure.persistence.entities.ClientJpaEntity;

@DataJpaTest
class SpringDataClientRepositoryTest {

    @Autowired
    private SpringDataClientRepository repository;

    @Test
    void findsClientByIdentificationId() {
        ClientJpaEntity entity = new ClientJpaEntity();
        entity.setId("client-1");
        entity.setIdentificationId("10101010");
        entity.setName("Miguel Lopez");
        entity.setEmail("miguel@example.com");
        entity.setPhone("3001234567");
        entity.setBirthDate(LocalDate.of(1999, 1, 10));
        entity.setAddress("Calle 123");
        entity.setClientType("NATURAL_PERSON_CLIENT");

        repository.save(entity);

        assertThat(repository.findByIdentificationId("10101010"))
                .map(ClientJpaEntity::getId)
                .contains("client-1");
    }
}
