package com.triacompany.academic.optimization.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.triacompany.academic.optimization.AcademicOptimizationReportResponse;
import com.triacompany.academic.optimization.AcademicOptimizationReportService;
import com.triacompany.academic.optimization.OptimizationRecommendationResponse;
import com.triacompany.academic.optimization.OptimizationScoreItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcademicOptimizationPdfService {

    private final AcademicOptimizationReportService reportService;

    public byte[] generatePdf(UUID researcherId) {
        AcademicOptimizationReportResponse report = reportService.generateReport(researcherId);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            addHeader(document);
            addTitle(document, "Relatório de Otimização Acadêmica");
            addResearcherInfo(document, report);
            addExecutiveSummary(document, report);
            addScoreOverview(document, report);
            addScoreItems(document, report);
            addRecommendations(document, report);
            addFooter(document, report);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível gerar o PDF do relatório acadêmico.");
        }
    }

    private void addHeader(Document document) throws DocumentException {
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(20, 40, 80));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

        Paragraph company = new Paragraph("TRIA Company", companyFont);
        company.setAlignment(Element.ALIGN_RIGHT);
        company.setSpacingAfter(2);
        document.add(company);

        Paragraph project = new Paragraph("Plataforma de Otimização Acadêmica para Universidades Angolanas", subtitleFont);
        project.setAlignment(Element.ALIGN_RIGHT);
        project.setSpacingAfter(20);
        document.add(project);
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));

        Paragraph paragraph = new Paragraph(title, titleFont);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(18);

        document.add(paragraph);
    }

    private void addResearcherInfo(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "1. Identificação do Pesquisador");

        addLine(document, "Nome", report.researcherName());
        addLine(document, "E-mail", report.researcherEmail());
        addLine(document, "Instituição", report.institution());
        addLine(document, "Departamento", report.department());
        addLine(document, "Título acadêmico", report.academicTitle());
        addLine(document, "ORCID", report.orcidId());

        addSpace(document, 10);
    }

    private void addExecutiveSummary(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "2. Resumo Executivo");

        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

        Paragraph summary = new Paragraph(nullSafe(report.executiveSummary()), textFont);
        summary.setAlignment(Element.ALIGN_JUSTIFIED);
        summary.setSpacingAfter(12);

        document.add(summary);
    }

    private void addScoreOverview(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "3. Pontuação Geral");

        Font scoreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, statusColor(report.overallStatus()));
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, statusColor(report.overallStatus()));

        Paragraph score = new Paragraph("Pontuação: " + report.overallScore() + "/100", scoreFont);
        score.setSpacingAfter(4);
        document.add(score);

        Paragraph status = new Paragraph("Status: " + nullSafe(report.overallStatus()), statusFont);
        status.setSpacingAfter(12);
        document.add(status);

        addLine(document, "Completude do perfil", report.profileCompletionPercentage() + "%");
        addLine(document, "Obras ORCID", String.valueOf(report.totalOrcidWorks()));
        addLine(document, "Obras OpenAlex", String.valueOf(report.totalOpenAlexWorks()));
        addLine(document, "Obras OpenAlex confirmadas", String.valueOf(report.confirmedOpenAlexWorks()));
        addLine(document, "Obras pendentes de revisão", String.valueOf(report.pendingReviewOpenAlexWorks()));
        addLine(document, "Validações Crossref", String.valueOf(report.totalCrossrefValidations()));
        addLine(document, "DOIs confirmados", String.valueOf(report.doiConfirmedCount()));

        addSpace(document, 10);
    }

    private void addScoreItems(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "4. Detalhamento da Pontuação");

        for (OptimizationScoreItemResponse item : report.scoreItems()) {
            Font itemTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(15, 23, 42));
            Font itemTextFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font itemStatusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, statusColor(item.status()));

            Paragraph title = new Paragraph(
                    item.label() + " — " + item.score() + "/" + item.maxScore(),
                    itemTitleFont
            );
            title.setSpacingBefore(6);
            title.setSpacingAfter(2);
            document.add(title);

            Paragraph status = new Paragraph("Status: " + item.status(), itemStatusFont);
            status.setSpacingAfter(2);
            document.add(status);

            Paragraph message = new Paragraph(nullSafe(item.message()), itemTextFont);
            message.setSpacingAfter(6);
            document.add(message);
        }

        addSpace(document, 10);
    }

    private void addRecommendations(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "5. Recomendações de Otimização");

        int index = 1;

        for (OptimizationRecommendationResponse recommendation : report.recommendations()) {
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, priorityColor(recommendation.priority()));
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

            Paragraph title = new Paragraph(
                    index + ". [" + recommendation.priority() + "] " + recommendation.area(),
                    titleFont
            );
            title.setSpacingBefore(6);
            title.setSpacingAfter(2);
            document.add(title);

            Paragraph text = new Paragraph(nullSafe(recommendation.recommendation()), textFont);
            text.setAlignment(Element.ALIGN_JUSTIFIED);
            text.setSpacingAfter(6);
            document.add(text);

            index++;
        }
    }

    private void addFooter(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSpace(document, 20);

        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

        String generatedAt = report.generatedAt() != null
                ? report.generatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "não informado";

        Paragraph footer = new Paragraph(
                "Relatório gerado automaticamente pela TRIA Company em " + generatedAt
                        + ". Este documento apoia a curadoria acadêmica institucional e não substitui revisão humana.",
                footerFont
        );

        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(20, 40, 80));

        Paragraph paragraph = new Paragraph(title, sectionFont);
        paragraph.setSpacingBefore(10);
        paragraph.setSpacingAfter(8);

        document.add(paragraph);
    }

    private void addLine(Document document, String label, String value) throws DocumentException {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(label + ": ", labelFont));
        paragraph.add(new Chunk(nullSafe(value), valueFont));
        paragraph.setSpacingAfter(3);

        document.add(paragraph);
    }

    private void addSpace(Document document, int spacing) throws DocumentException {
        Paragraph paragraph = new Paragraph(" ");
        paragraph.setSpacingAfter(spacing);
        document.add(paragraph);
    }

    private Color statusColor(String status) {
        if ("EXCELENTE".equalsIgnoreCase(status)) {
            return new Color(22, 163, 74);
        }

        if ("BOM".equalsIgnoreCase(status)) {
            return new Color(37, 99, 235);
        }

        if ("EM_OTIMIZACAO".equalsIgnoreCase(status)) {
            return new Color(245, 158, 11);
        }

        return new Color(220, 38, 38);
    }

    private Color priorityColor(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return new Color(220, 38, 38);
        }

        if ("MEDIUM".equalsIgnoreCase(priority)) {
            return new Color(245, 158, 11);
        }

        return new Color(22, 163, 74);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "não informado" : value;
    }
}