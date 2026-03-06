package io.github.mavarazo.vision.shared.testing;

import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration
@AutoConfigureTestEntityManager
public class TestDataManagerTestConfig {

    @Bean
    public TestDataManager testDataManager(final JdbcTemplate jdbcTemplate, final TestEntityManager testEntityManager) {
        return new TestDataManager(jdbcTemplate, testEntityManager);
    }
}
