package ru.julia.currencyexchange.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.julia.currencyexchange.application.service.ReportService;
import ru.julia.currencyexchange.domain.enums.StatusEnum;
import ru.julia.currencyexchange.domain.model.Report;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/report")
public class ReportViewController {

    private final ReportService reportService;

    public ReportViewController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{id}")
    public String viewReport(@PathVariable String id, Model model) {
        Report report = reportService.getReport(id);

        model.addAttribute("status", report.getStatus().name());

        if (report.getStatus() == StatusEnum.COMPLETED) {
            try {
                Path file = Paths.get("src/main/resources/static" + report.getFilePath());
                String html = Files.readString(file);
                model.addAttribute("htmlContent", html);
            } catch (Exception e) {
                model.addAttribute("status", "FAILED");
            }
        }

        model.addAttribute("allReports", reportService.getAllReports());

        return "report";
    }

    @GetMapping("/latest")
    public String redirectToLatestReport() {
        Report latest = reportService.getLatestReportOrNull();

        if (latest == null) {
            String reportId = reportService.saveReport();
            reportService.generateReportAsync(reportId);
            return "redirect:/report/" + reportId;
        }

        return "redirect:/report/" + latest.getId();
    }
}
