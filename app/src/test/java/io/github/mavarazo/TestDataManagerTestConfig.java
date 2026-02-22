package io.github.mavarazo;

import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@AutoConfigureTestEntityManager
public class TestDataManagerTestConfig {

    @Bean
    public TestDataManager testDataManager(final TestEntityManager testEntityManager) {
        return new TestDataManager(testEntityManager);
    }
}
