package io.github.mavarazo.vision.shared.testing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class TestDataManager {

    private final JdbcTemplate jdbcTemplate;
    private final TestEntityManager testEntityManager;

    public TestDataManager(final JdbcTemplate jdbcTemplate, final TestEntityManager testEntityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.testEntityManager = testEntityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> List<T> findAll(final Class<T> clazz) {
        final EntityManager em = testEntityManager.getEntityManager();
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(clazz);
        final Root<T> rootEntry = cq.from(clazz);
        final CriteriaQuery<T> all = cq.select(rootEntry);
        final TypedQuery<T> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> UUID persistAndGetId(final T entity) {
        return testEntityManager.persistAndGetId(entity, UUID.class);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void truncateAll() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        final List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                String.class
        );

        for (final String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        testEntityManager.clear();
    }
}
