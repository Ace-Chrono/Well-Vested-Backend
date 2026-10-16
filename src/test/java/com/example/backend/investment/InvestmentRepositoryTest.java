package com.example.backend.investment;

import com.example.backend.investment.entity.AssetType;
import com.example.backend.investment.entity.Investment;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(InvestmentRepositoryTest.Config.class)
@Transactional
class InvestmentRepositoryTest {
    @Autowired private InvestmentRepository repository;
    @Autowired private com.example.backend.chat.ChatRepository chats;
    @Autowired private com.example.backend.transaction.TransactionRepository transactions;
    @Autowired private com.example.backend.insight.InsightRepository insights;
    @Autowired private EntityManager entityManager;
    @Autowired private JpaTransactionManager transactionManager;

    @Test
    void persistsReloadsUpdatesAndDeletesHolding() {
        Investment holding = new Investment(null, "BTC", "Bitcoin", AssetType.CRYPTO, 0.12345678, 91250.12, -1.5, null, null, null, null);
        Investment saved = repository.saveAndFlush(holding);
        assertNotNull(saved.getInvestmentId());
        entityManager.clear();
        Investment loaded = repository.findById(saved.getInvestmentId()).orElseThrow();
        assertEquals(AssetType.CRYPTO, loaded.getAssetType());
        assertEquals(0.12345678, loaded.getShares());
        assertEquals(91250.12, loaded.getPrice());
        loaded.setShares(0.5);
        repository.saveAndFlush(loaded);
        entityManager.clear();
        assertEquals(0.5, repository.findById(saved.getInvestmentId()).orElseThrow().getShares());
        repository.deleteById(saved.getInvestmentId());
        repository.flush();
        entityManager.clear();
        assertFalse(repository.existsById(saved.getInvestmentId()));
    }

    @Test
    void persistsAndReplacesInsightSnapshotsByScopeAndCurrency() {
        com.example.backend.insight.entity.Insight insight = new com.example.backend.insight.entity.Insight();
        insight.setScope("SPENDING");
        insight.setType("SPENDING_TOTAL");
        insight.setIsoCurrencyCode("USD");
        insight.setValue(123.45);
        insight.setGeneratedAt(java.time.Instant.now());
        insights.saveAndFlush(insight);
        entityManager.clear();
        var loaded = insights.findByScopeAndIsoCurrencyCodeOrderByTypeAscSubjectAsc("SPENDING", "USD");
        assertEquals(1, loaded.size());
        assertEquals(123.45, loaded.get(0).getValue());
        assertNotNull(loaded.get(0).getInsightId());
        insights.deleteByScopeAndIsoCurrencyCode("SPENDING", "EUR");
        assertEquals(1, insights.count());
        insights.deleteByScopeAndIsoCurrencyCode("SPENDING", "USD");
        insights.flush();
        entityManager.clear();
        assertTrue(insights.findByScopeAndIsoCurrencyCodeOrderByTypeAscSubjectAsc("SPENDING", "USD").isEmpty());
    }

    @Test
    void chatPersistsAndReloadsAndContextQueriesInitialize() {
        var turn = new com.example.backend.chat.entity.ChatTurn();
        var conversation = java.util.UUID.randomUUID();
        turn.setConversationId(conversation);
        turn.setMessage("Explain spending");
        turn.setReply("No data yet.");
        turn.setCurrency("USD");
        turn.setCreatedAt(java.time.Instant.now());
        chats.saveAndFlush(turn);
        entityManager.clear();
        var history = chats.findTop6ByConversationIdOrderByCreatedAtDesc(conversation);
        assertEquals(1, history.size());
        assertNotNull(history.get(0).getTurnId());
        assertEquals("No data yet.", history.get(0).getReply());
        var page = org.springframework.data.domain.PageRequest.of(0, 100);
        assertTrue(repository.findByIsoCurrencyCodeOrderByInvestmentIdAsc("USD", page).isEmpty());
        assertTrue(transactions.findByIsoCurrencyCodeAndDateBetweenOrderByDateDescTransactionIdAsc(
            "USD", java.time.LocalDate.of(2026, 9, 1), java.time.LocalDate.of(2026, 9, 30), page).isEmpty());
        assertTrue(insights.findByIsoCurrencyCodeOrderByGeneratedAtDescInsightIdAsc("USD", page).isEmpty());
    }

    @Configuration
    @EnableJpaRepositories(basePackageClasses = {InvestmentRepository.class, com.example.backend.insight.InsightRepository.class, com.example.backend.chat.ChatRepository.class, com.example.backend.transaction.TransactionRepository.class})
    @EnableTransactionManagement
    static class Config {
        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(Investment.class.getPackageName(), "com.example.backend.insight.entity", "com.example.backend.chat.entity", "com.example.backend.transaction.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }

        @Bean
        JpaTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        @Bean
        EntityManager entityManager(jakarta.persistence.EntityManagerFactory factory) {
            return org.springframework.orm.jpa.SharedEntityManagerCreator.createSharedEntityManager(factory);
        }
    }
}
