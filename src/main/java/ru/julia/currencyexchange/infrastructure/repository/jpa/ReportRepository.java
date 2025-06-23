package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import ru.julia.currencyexchange.domain.model.Report;

import java.util.Optional;

public interface ReportRepository extends CrudRepository<Report, String> {
    Optional<Report> findTopByOrderByCreatedAtDesc();
}
