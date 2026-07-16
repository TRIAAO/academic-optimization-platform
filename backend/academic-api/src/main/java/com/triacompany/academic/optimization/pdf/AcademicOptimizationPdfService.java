package com.triacompany.academic.optimization.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.triacompany.academic.optimization.AcademicOptimizationReportResponse;
import com.triacompany.academic.optimization.AcademicOptimizationReportService;
import com.triacompany.academic.optimization.OptimizationRecommendationResponse;
import com.triacompany.academic.optimization.OptimizationScoreItemResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AcademicOptimizationPdfService {

    private static final Color NAVY = new Color(3, 12, 38);
    private static final Color BLUE = new Color(29, 78, 216);
    private static final Color BLUE_LIGHT = new Color(239, 246, 255);
    private static final Color SLATE = new Color(15, 23, 42);
    private static final Color MUTED = new Color(71, 85, 105);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final Color WHITE = Color.WHITE;
    private static final String LOGO_RESOURCE = "/branding/imetro-logo.png";
    private static final String REGULAR_FONT_RESOURCE = "/branding/fonts/DejaVuSans.ttf";
    private static final String BOLD_FONT_RESOURCE = "/branding/fonts/DejaVuSans-Bold.ttf";

    private final AcademicOptimizationReportService reportService;
    private BaseFont regularBaseFont;
    private BaseFont boldBaseFont;

    public AcademicOptimizationPdfService(AcademicOptimizationReportService reportService) {
        this.reportService = reportService;
    }

    public byte[] generatePdf(UUID researcherId) {
        return renderReport(reportService.generateReport(researcherId));
    }

    byte[] renderReport(AcademicOptimizationReportResponse report) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 46, 46, 48, 52);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            writer.setPageEvent(new InstitutionalPageEvent(baseFont(false), baseFont(true)));

            document.addTitle("Relatório de Otimização Acadêmica - " + nullSafe(report.researcherName()));
            document.addSubject("Diagnóstico e plano de ação acadêmico institucional");
            document.addAuthor("IMETRO / TRIA Company");
            document.addCreator("Plataforma de Otimização Acadêmica");
            document.open();

            addInstitutionalHeader(document);
            addHero(document, report);
            addResearcherInfo(document, report);
            addExecutiveSummary(document, report);
            addScoreOverview(document, report);
            addScoreItems(document, report);
            addRecommendations(document, report);
            addIntegrityNotice(document, report);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível gerar o PDF do relatório acadêmico.", exception);
        }
    }

    private void addInstitutionalHeader(Document document) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{0.20f, 0.80f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(10);

        PdfPCell logoCell = baseCell(WHITE, 0);
        logoCell.setPadding(0);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Image logo = loadLogo();
        if (logo != null) {
            logo.scaleToFit(78, 52);
            logo.setAlignment(Element.ALIGN_LEFT);
            logoCell.addElement(logo);
        } else {
            logoCell.addElement(new Paragraph("IMETRO", font(FontFactory.HELVETICA_BOLD, 13, BLUE)));
        }

        PdfPCell identityCell = baseCell(WHITE, 0);
        identityCell.setPadding(0);
        identityCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph institution = new Paragraph(
                "INSTITUTO SUPERIOR POLITÉCNICO METROPOLITANO DE ANGOLA / IMETRO",
                font(FontFactory.HELVETICA_BOLD, 9, BLUE)
        );
        institution.setAlignment(Element.ALIGN_RIGHT);
        institution.setLeading(12);
        identityCell.addElement(institution);

        Paragraph platform = new Paragraph(
                "Plataforma de Otimização Acadêmica",
                font(FontFactory.HELVETICA_BOLD, 12, NAVY)
        );
        platform.setAlignment(Element.ALIGN_RIGHT);
        platform.setSpacingBefore(4);
        identityCell.addElement(platform);

        Paragraph executor = new Paragraph(
                "Gestão institucional de perfis e produção científica",
                font(FontFactory.HELVETICA, 8, MUTED)
        );
        executor.setAlignment(Element.ALIGN_RIGHT);
        executor.setSpacingBefore(2);
        identityCell.addElement(executor);

        header.addCell(logoCell);
        header.addCell(identityCell);
        document.add(header);

        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingAfter(14);
        PdfPCell ruleCell = baseCell(BLUE, 0);
        ruleCell.setFixedHeight(3);
        rule.addCell(ruleCell);
        document.add(rule);
    }

    private void addHero(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        PdfPTable hero = new PdfPTable(new float[]{0.72f, 0.28f});
        hero.setWidthPercentage(100);
        hero.setKeepTogether(true);
        hero.setSpacingAfter(18);

        PdfPCell titleCell = baseCell(NAVY, 0);
        titleCell.setPadding(18);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph eyebrow = new Paragraph(
                "DIAGNÓSTICO INSTITUCIONAL",
                font(FontFactory.HELVETICA_BOLD, 8, new Color(147, 197, 253))
        );
        eyebrow.setSpacingAfter(7);
        titleCell.addElement(eyebrow);

        Paragraph title = new Paragraph(
                "Relatório de Otimização Acadêmica",
                font(FontFactory.HELVETICA_BOLD, 20, WHITE)
        );
        title.setLeading(24);
        titleCell.addElement(title);

        Paragraph researcher = new Paragraph(
                nullSafe(report.researcherName()),
                font(FontFactory.HELVETICA, 10, new Color(203, 213, 225))
        );
        researcher.setSpacingBefore(8);
        titleCell.addElement(researcher);

        PdfPCell scoreCell = baseCell(statusColor(report.overallStatus()), 0);
        scoreCell.setPadding(14);
        scoreCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        scoreCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph scoreLabel = new Paragraph(
                "SCORE ACADÊMICO",
                font(FontFactory.HELVETICA_BOLD, 8, WHITE)
        );
        scoreLabel.setAlignment(Element.ALIGN_CENTER);
        scoreLabel.setSpacingAfter(4);
        scoreCell.addElement(scoreLabel);

        Paragraph score = new Paragraph(
                String.valueOf(report.overallScore()),
                font(FontFactory.HELVETICA_BOLD, 30, WHITE)
        );
        score.setAlignment(Element.ALIGN_CENTER);
        scoreCell.addElement(score);

        Paragraph total = new Paragraph("de 100", font(FontFactory.HELVETICA, 8, WHITE));
        total.setAlignment(Element.ALIGN_CENTER);
        total.setSpacingAfter(6);
        scoreCell.addElement(total);

        Paragraph status = new Paragraph(
                statusLabel(report.overallStatus()),
                font(FontFactory.HELVETICA_BOLD, 9, WHITE)
        );
        status.setAlignment(Element.ALIGN_CENTER);
        scoreCell.addElement(status);

        hero.addCell(titleCell);
        hero.addCell(scoreCell);
        document.add(hero);
    }

    private void addResearcherInfo(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "01", "Identificação do pesquisador");

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setKeepTogether(true);
        info.setSpacingAfter(10);

        info.addCell(infoCell("Nome", report.researcherName()));
        info.addCell(infoCell("E-mail", report.researcherEmail()));
        info.addCell(infoCell("Instituição", report.institution()));
        info.addCell(infoCell("Departamento", report.department()));
        info.addCell(infoCell("Título acadêmico", report.academicTitle()));
        info.addCell(infoCell("ORCID", report.orcidId()));

        document.add(info);
    }

    private void addExecutiveSummary(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "02", "Resumo executivo");

        PdfPTable summaryTable = new PdfPTable(1);
        summaryTable.setWidthPercentage(100);
        summaryTable.setKeepTogether(true);
        summaryTable.setSpacingAfter(10);

        PdfPCell summaryCell = baseCell(BLUE_LIGHT, 1);
        summaryCell.setBorderColor(new Color(191, 219, 254));
        summaryCell.setPadding(14);

        Paragraph summary = new Paragraph(
                nullSafe(report.executiveSummary()),
                font(FontFactory.HELVETICA, 10, new Color(30, 64, 175))
        );
        summary.setAlignment(Element.ALIGN_JUSTIFIED);
        summary.setLeading(15);
        summaryCell.addElement(summary);

        summaryTable.addCell(summaryCell);
        document.add(summaryTable);
    }

    private void addScoreOverview(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "03", "Panorama acadêmico");

        PdfPTable stats = new PdfPTable(4);
        stats.setWidthPercentage(100);
        stats.setKeepTogether(true);
        stats.setSpacingAfter(12);

        stats.addCell(statCell("Perfil acadêmico", report.profileCompletionPercentage() + "%", new Color(124, 58, 237)));
        stats.addCell(statCell("Obras ORCID", String.valueOf(report.totalOrcidWorks()), new Color(5, 150, 105)));
        stats.addCell(statCell("OpenAlex confirmadas", report.confirmedOpenAlexWorks() + " / " + report.totalOpenAlexWorks(), BLUE));
        stats.addCell(statCell("DOIs confirmados", report.doiConfirmedCount() + " / " + report.totalCrossrefValidations(), new Color(217, 119, 6)));

        document.add(stats);
    }

    private void addScoreItems(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        document.newPage();
        addSectionTitle(document, "04", "Composição do score");

        for (OptimizationScoreItemResponse item : report.scoreItems()) {
            PdfPTable card = new PdfPTable(1);
            card.setWidthPercentage(100);
            card.setKeepTogether(true);
            card.setSpacingAfter(8);

            PdfPCell cardCell = baseCell(WHITE, 1);
            cardCell.setBorderColor(BORDER);
            cardCell.setPadding(12);

            PdfPTable itemHeader = new PdfPTable(new float[]{0.74f, 0.26f});
            itemHeader.setWidthPercentage(100);

            PdfPCell titleCell = baseCell(WHITE, 0);
            titleCell.setPadding(0);
            Paragraph itemTitle = new Paragraph(
                    item.label(),
                    font(FontFactory.HELVETICA_BOLD, 10, SLATE)
            );
            titleCell.addElement(itemTitle);

            Paragraph itemScore = new Paragraph(
                    item.score() + " / " + item.maxScore(),
                    font(FontFactory.HELVETICA_BOLD, 9, MUTED)
            );
            itemScore.setSpacingBefore(3);
            titleCell.addElement(itemScore);

            PdfPCell statusCell = baseCell(statusColor(item.status()), 0);
            statusCell.setPadding(7);
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            statusCell.addElement(centeredParagraph(
                    statusLabel(item.status()),
                    font(FontFactory.HELVETICA_BOLD, 8, WHITE)
            ));

            itemHeader.addCell(titleCell);
            itemHeader.addCell(statusCell);
            cardCell.addElement(itemHeader);

            Paragraph message = new Paragraph(
                    nullSafe(item.message()),
                    font(FontFactory.HELVETICA, 9, MUTED)
            );
            message.setLeading(13);
            message.setSpacingBefore(8);
            message.setSpacingAfter(8);
            cardCell.addElement(message);
            cardCell.addElement(progressBar(item.score(), item.maxScore(), statusColor(item.status())));

            card.addCell(cardCell);
            document.add(card);
        }
    }

    private void addRecommendations(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        document.newPage();
        addSectionTitle(document, "05", "Plano de ação priorizado");

        int index = 1;

        for (OptimizationRecommendationResponse recommendation : report.recommendations()) {
            PdfPTable card = new PdfPTable(new float[]{0.21f, 0.79f});
            card.setWidthPercentage(100);
            card.setKeepTogether(true);
            card.setSpacingAfter(8);

            PdfPCell priorityCell = baseCell(priorityColor(recommendation.priority()), 0);
            priorityCell.setPadding(10);
            priorityCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            priorityCell.addElement(centeredParagraph(
                    String.format("%02d", index),
                    font(FontFactory.HELVETICA_BOLD, 15, WHITE)
            ));

            Paragraph priority = centeredParagraph(
                    priorityLabel(recommendation.priority()),
                    font(FontFactory.HELVETICA_BOLD, 8, WHITE)
            );
            priority.setSpacingBefore(4);
            priorityCell.addElement(priority);

            PdfPCell actionCell = baseCell(SURFACE, 1);
            actionCell.setBorderColor(BORDER);
            actionCell.setPadding(12);

            Paragraph area = new Paragraph(
                    nullSafe(recommendation.area()),
                    font(FontFactory.HELVETICA_BOLD, 10, SLATE)
            );
            area.setSpacingAfter(5);
            actionCell.addElement(area);

            Paragraph text = new Paragraph(
                    nullSafe(recommendation.recommendation()),
                    font(FontFactory.HELVETICA, 9, MUTED)
            );
            text.setLeading(13);
            actionCell.addElement(text);

            card.addCell(priorityCell);
            card.addCell(actionCell);
            document.add(card);
            index++;
        }
    }

    private void addIntegrityNotice(
            Document document,
            AcademicOptimizationReportResponse report
    ) throws DocumentException {
        addSectionTitle(document, "06", "Governança e integridade");

        PdfPTable notice = new PdfPTable(1);
        notice.setWidthPercentage(100);
        notice.setKeepTogether(true);

        PdfPCell noticeCell = baseCell(new Color(255, 251, 235), 1);
        noticeCell.setBorderColor(new Color(253, 230, 138));
        noticeCell.setPadding(13);

        Paragraph title = new Paragraph(
                "Google Acadêmico: procedimento exclusivamente manual",
                font(FontFactory.HELVETICA_BOLD, 10, new Color(146, 64, 14))
        );
        title.setSpacingAfter(5);
        noticeCell.addElement(title);

        Paragraph text = new Paragraph(
                "A plataforma não automatiza, não acessa, não altera e não coleta dados diretamente do Google Acadêmico. "
                        + "As recomendações deste relatório apoiam a curadoria institucional e não substituem a validação humana.",
                font(FontFactory.HELVETICA, 9, new Color(146, 64, 14))
        );
        text.setLeading(13);
        noticeCell.addElement(text);

        Paragraph generatedAt = new Paragraph(
                "Documento emitido em " + formattedGeneratedAt(report) + ".",
                font(FontFactory.HELVETICA, 8, new Color(161, 98, 7))
        );
        generatedAt.setSpacingBefore(7);
        noticeCell.addElement(generatedAt);

        notice.addCell(noticeCell);
        document.add(notice);
    }

    private void addSectionTitle(Document document, String number, String title) throws DocumentException {
        PdfPTable section = new PdfPTable(new float[]{0.10f, 0.90f});
        section.setWidthPercentage(100);
        section.setKeepTogether(true);
        section.setSpacingBefore(7);
        section.setSpacingAfter(8);

        PdfPCell numberCell = baseCell(BLUE, 0);
        numberCell.setPadding(7);
        numberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numberCell.addElement(centeredParagraph(
                number,
                font(FontFactory.HELVETICA_BOLD, 9, WHITE)
        ));

        PdfPCell titleCell = baseCell(WHITE, 0);
        titleCell.setPadding(7);
        titleCell.setPaddingLeft(11);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.addElement(new Paragraph(
                title,
                font(FontFactory.HELVETICA_BOLD, 12, NAVY)
        ));

        section.addCell(numberCell);
        section.addCell(titleCell);
        document.add(section);
    }

    private PdfPCell infoCell(String label, String value) {
        PdfPCell cell = baseCell(SURFACE, 1);
        cell.setBorderColor(WHITE);
        cell.setBorderWidth(3);
        cell.setPadding(10);

        Paragraph labelParagraph = new Paragraph(
                label.toUpperCase(),
                font(FontFactory.HELVETICA_BOLD, 7, MUTED)
        );
        labelParagraph.setSpacingAfter(4);
        cell.addElement(labelParagraph);
        cell.addElement(new Paragraph(nullSafe(value), font(FontFactory.HELVETICA_BOLD, 9, SLATE)));
        return cell;
    }

    private PdfPCell statCell(String label, String value, Color accent) {
        PdfPCell cell = baseCell(SURFACE, 1);
        cell.setBorderColor(WHITE);
        cell.setBorderWidth(3);
        cell.setPadding(10);

        Paragraph valueParagraph = new Paragraph(value, font(FontFactory.HELVETICA_BOLD, 17, accent));
        valueParagraph.setSpacingAfter(5);
        cell.addElement(valueParagraph);

        Paragraph labelParagraph = new Paragraph(label, font(FontFactory.HELVETICA_BOLD, 8, MUTED));
        labelParagraph.setLeading(10);
        cell.addElement(labelParagraph);
        return cell;
    }

    private PdfPTable progressBar(Integer score, Integer maxScore, Color color) throws DocumentException {
        int safeScore = Math.max(0, score == null ? 0 : score);
        int safeMaximum = Math.max(1, maxScore == null ? 1 : maxScore);
        int remaining = Math.max(0, safeMaximum - safeScore);

        PdfPTable bar = new PdfPTable(new float[]{Math.max(0.1f, safeScore), Math.max(0.1f, remaining)});
        bar.setWidthPercentage(100);

        PdfPCell completed = baseCell(color, 0);
        completed.setFixedHeight(5);
        bar.addCell(completed);

        PdfPCell pending = baseCell(BORDER, 0);
        pending.setFixedHeight(5);
        bar.addCell(pending);
        return bar;
    }

    private PdfPCell baseCell(Color background, int borderWidth) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(background);
        cell.setBorderWidth(borderWidth);
        cell.setBorderColor(BORDER);
        return cell;
    }

    private Paragraph centeredParagraph(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    private Image loadLogo() {
        try (InputStream inputStream = getClass().getResourceAsStream(LOGO_RESOURCE)) {
            if (inputStream == null) {
                return null;
            }

            return Image.getInstance(inputStream.readAllBytes());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Font font(String family, float size, Color color) {
        boolean bold = family != null && family.toUpperCase().contains("BOLD");
        boolean italic = family != null && family.toUpperCase().contains("OBLIQUE");
        return new Font(baseFont(bold), size, italic ? Font.ITALIC : Font.NORMAL, color);
    }

    private synchronized BaseFont baseFont(boolean bold) {
        if (bold && boldBaseFont == null) {
            boldBaseFont = loadBaseFont(BOLD_FONT_RESOURCE, "DejaVuSans-Bold.ttf");
        }

        if (!bold && regularBaseFont == null) {
            regularBaseFont = loadBaseFont(REGULAR_FONT_RESOURCE, "DejaVuSans.ttf");
        }

        return bold ? boldBaseFont : regularBaseFont;
    }

    private BaseFont loadBaseFont(String resource, String logicalName) {
        try (InputStream inputStream = getClass().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IllegalStateException("Fonte institucional não encontrada: " + resource);
            }

            return BaseFont.createFont(
                    logicalName,
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED,
                    true,
                    inputStream.readAllBytes(),
                    null
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível carregar a fonte institucional.", exception);
        }
    }

    private Color statusColor(String status) {
        if ("EXCELENTE".equalsIgnoreCase(status)) {
            return new Color(5, 150, 105);
        }

        if ("BOM".equalsIgnoreCase(status)) {
            return BLUE;
        }

        if ("EM_OTIMIZACAO".equalsIgnoreCase(status)) {
            return new Color(217, 119, 6);
        }

        return new Color(220, 38, 38);
    }

    private Color priorityColor(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return new Color(220, 38, 38);
        }

        if ("MEDIUM".equalsIgnoreCase(priority)) {
            return new Color(217, 119, 6);
        }

        return new Color(5, 150, 105);
    }

    private String statusLabel(String status) {
        if ("EM_OTIMIZACAO".equalsIgnoreCase(status)) {
            return "EM OTIMIZAÇÃO";
        }

        if ("CRITICO".equalsIgnoreCase(status)) {
            return "CRÍTICO";
        }

        return nullSafe(status).replace('_', ' ');
    }

    private String priorityLabel(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return "ALTA";
        }

        if ("MEDIUM".equalsIgnoreCase(priority)) {
            return "MÉDIA";
        }

        return "BAIXA";
    }

    private String formattedGeneratedAt(AcademicOptimizationReportResponse report) {
        return report.generatedAt() == null
                ? "data não informada"
                : report.generatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "Não informado" : value;
    }

    private static class InstitutionalPageEvent extends PdfPageEventHelper {

        private final BaseFont regularFont;
        private final BaseFont boldFont;

        private InstitutionalPageEvent(BaseFont regularFont, BaseFont boldFont) {
            this.regularFont = regularFont;
            this.boldFont = boldFont;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            float footerY = 31;

            canvas.saveState();
            canvas.setColorStroke(BORDER);
            canvas.setLineWidth(0.7f);
            canvas.moveTo(document.left(), footerY + 9);
            canvas.lineTo(document.right(), footerY + 9);
            canvas.stroke();

            Font footerFont = new Font(regularFont, 7, Font.NORMAL, MUTED);
            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_LEFT,
                    new Phrase("IMETRO | Plataforma de Otimização Acadêmica | Executado por TRIA Company", footerFont),
                    document.left(),
                    footerY,
                    0
            );
            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), footerFont),
                    document.right(),
                    footerY,
                    0
            );

            if (writer.getPageNumber() > 1) {
                Font headerFont = new Font(boldFont, 7, Font.NORMAL, BLUE);
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_LEFT,
                        new Phrase("RELATÓRIO DE OTIMIZAÇÃO ACADÊMICA", headerFont),
                        document.left(),
                        PageSize.A4.getHeight() - 25,
                        0
                );
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_RIGHT,
                        new Phrase("IMETRO", headerFont),
                        document.right(),
                        PageSize.A4.getHeight() - 25,
                        0
                );
            }

            canvas.restoreState();
        }
    }
}
