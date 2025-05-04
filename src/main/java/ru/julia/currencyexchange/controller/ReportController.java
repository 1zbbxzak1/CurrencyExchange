package ru.julia.currencyexchange.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.julia.currencyexchange.application.service.ReportService;
import ru.julia.currencyexchange.domain.enums.StatusEnum;
import ru.julia.currencyexchange.domain.model.Report;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;


    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createReport() {
        String reportId = reportService.saveReport();
        reportService.generateReportAsync(reportId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("reportId", reportId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReportContent(@PathVariable String id) {
        Report report = reportService.getReport(id);

        if (report.getStatus() == StatusEnum.CREATED) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "pending", "message", "Отчет еще формируется"));
        }

        if (report.getStatus() == StatusEnum.FAILED) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "failed", "message", "Ошибка при формировании отчета"));
        }

        try {
            Path file = Paths.get("src/main/resources/static" + report.getFilePath());
            String content = Files.readString(file);
            return ResponseEntity.ok().body(content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Не удалось прочитать файл отчета"));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestReportContent() {
        try {
            Report report = reportService.getLatestReportOrNull();

            if (report.getStatus() == StatusEnum.CREATED) {
                return ResponseEntity.ok(Map.of("status", "pending", "message", "Отчет еще формируется"));
            }

            if (report.getStatus() == StatusEnum.FAILED) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "failed", "message", "Ошибка при формировании отчета"));
            }

            Path file = Paths.get("src/main/resources/static" + report.getFilePath());
            String content = Files.readString(file);
            return ResponseEntity.ok(content);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Не удалось получить последний отчет"));
        }
    }

}
