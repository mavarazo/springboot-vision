package io.github.mavarazo;

import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class TestDataManager {

    private final TestEntityManager testEntityManager;

    public TestDataManager(final TestEntityManager testEntityManager) {
        this.testEntityManager = testEntityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> UUID persistAndGetId(final T entity) {
        return testEntityManager.persistAndGetId(entity, UUID.class);
    }
}
