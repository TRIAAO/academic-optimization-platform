package com.triacompany.academic.optimization.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/optimization-reports")
@RequiredArgsConstructor
public class AcademicOptimizationPdfController {

    private final AcademicOptimizationPdfService pdfService;

    @GetMapping("/researchers/{researcherId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public ResponseEntity<byte[]> generatePdf(@PathVariable UUID researcherId) {
        byte[] pdfBytes = pdfService.generatePdf(researcherId);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "relatorio_otimizacao_academica_" + researcherId + "_" + timestamp + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}