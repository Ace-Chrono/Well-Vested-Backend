package com.example.backend.transaction;

import com.example.backend.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    /* What the code would have looked like without JPA
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Transaction save(Transaction transaction) {
        entityManager.persist(transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(entityManager.find(Transaction.class, id));
    }

    @Override
    public List<Transaction> findAll() {
        return entityManager.createQuery("SELECT t FROM Transaction t", Transaction.class)
                .getResultList();
    }

    @Override
    public void deleteById(String id) {
        Transaction t = entityManager.find(Transaction.class, id);
        if (t != null) entityManager.remove(t);
    }

    // ... and so on
     */
}
