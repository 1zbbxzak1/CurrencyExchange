package ru.julia.currencyexchange.infrastructure.repository.criteria;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomConversionRepositoryImpl implements CustomConversionRepository {
    EntityManager entityManager;

    @Autowired
    public CustomConversionRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<CurrencyConversion> findConversionByUserId(String userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CurrencyConversion> query = cb.createQuery(CurrencyConversion.class);
        Root<CurrencyConversion> root = query.from(CurrencyConversion.class);

        query.select(root).where(cb.equal(root.get("user").get("id"), userId));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<List<CurrencyConversion>> findConversionByAmountRange(Double minAmount, Double maxAmount) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CurrencyConversion> query = cb.createQuery(CurrencyConversion.class);
        Root<CurrencyConversion> root = query.from(CurrencyConversion.class);

        Predicate minCondition = cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
        Predicate maxCondition = cb.lessThanOrEqualTo(root.get("amount"), maxAmount);

        query.select(root).where(cb.and(minCondition, maxCondition));

        List<CurrencyConversion> result = entityManager.createQuery(query).getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
}
