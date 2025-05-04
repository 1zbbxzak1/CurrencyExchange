package ru.julia.currencyexchange.application.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.domain.enums.StatusEnum;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.Report;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.ReportRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;

    private final String reportDir = "src/main/resources/static/reports";

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         CurrencyRepository currencyRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    public String saveReport() {
        Report report = new Report();
        report.setStatus(StatusEnum.CREATED);
        report.setFilePath("");
        reportRepository.save(report);

        return report.getId();
    }

    @Async
    public CompletableFuture<Void> generateReportAsync(String reportId) {
        return CompletableFuture.runAsync(() -> {
            Report report = getReport(reportId);
            long totalStart = System.currentTimeMillis();

            final long[] userCount = new long[1];
            final long[] userTime = new long[1];

            final List<Currency>[] currencies = new List[1];
            final long[] currencyTime = new long[1];

            Thread userThread = new Thread(() -> {
                long start = System.currentTimeMillis();
                userCount[0] = userRepository.count();
                userTime[0] = System.currentTimeMillis() - start;
            });

            Thread currencyThread = new Thread(() -> {
                long start = System.currentTimeMillis();
                currencies[0] = (List<Currency>) currencyRepository.findAll();
                currencyTime[0] = System.currentTimeMillis() - start;
            });

            try {
                userThread.start();
                currencyThread.start();

                userThread.join();
                currencyThread.join();

                long totalDuration = System.currentTimeMillis() - totalStart;

                String html = generateHtmlReport(
                        userCount[0],
                        currencies[0],
                        userTime[0],
                        currencyTime[0],
                        totalDuration,
                        report.getCreatedAt()
                );

                String filename = "report-" + UUID.randomUUID() + ".html";
                Path filePath = Paths.get(reportDir, filename);
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, html);

                report.setFilePath("/reports/" + filename);
                report.setStatus(StatusEnum.COMPLETED);
                reportRepository.save(report);
            } catch (Exception e) {
                report.setStatus(StatusEnum.FAILED);
                report.setFilePath("Ошибка: " + e.getMessage());
                reportRepository.save(report);
            }
        });
    }

    public Report getReport(String id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    private String generateHtmlReport(long userCount, List<Currency> currencies,
                                      long userTime, long currencyTime, long totalTime, LocalDateTime time) {
        StringBuilder html = new StringBuilder();
        html.append("<p><b>Создан:</b> ").append(time).append("</p>");
        html.append("<p><b>Пользователей зарегистрировано:</b> ").append(userCount).append("</p>");
        html.append("<p><b>Время подсчета пользователей:</b> ").append(userTime).append(" мс</p>");
        html.append("<p><b>Время получения списка валют:</b> ").append(currencyTime).append(" мс</p>");
        html.append("<p><b>Общее время формирования отчета:</b> ").append(totalTime).append(" мс</p>");
        html.append("<h2>Список валют</h2>");
        html.append("<table border='1'><tr><th>Код</th><th>Название</th><th>Курс</th></tr>");
        for (Currency currency : currencies) {
            html.append("<tr><td>")
                    .append(currency.getCode())
                    .append("</td><td>")
                    .append(currency.getName())
                    .append("</td><td>")
                    .append(currency.getExchangeRate())
                    .append("</td></tr>");
        }
        html.append("</table>");
        html.append("</body></html>");
        return html.toString();
    }

    public Report getLatestReportOrNull() {
        return reportRepository.findTopByOrderByCreatedAtDesc().orElse(null);
    }

    public List<Report> getAllReports() {
        return (List<Report>) reportRepository.findAll();
    }
}
