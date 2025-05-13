package com.email_service.email_service.builders;

import com.email_service.email_service.models.AccreditationDataForPdf;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class PdfGenerator {
    private static final DateTimeFormatter CUSTOM_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter CUSTOM_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateAccreditationPdf(AccreditationDataForPdf data) throws IOException {
        if (data == null) {
            log.error("Los datos para generar el PDF son nulos.");
            throw new IllegalArgumentException("Accreditation data cannot be null for PDF generation.");
        }
        log.info("Generando PDF para acreditación ID: {}", data.getAccreditationId());

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
            PDType1Font fontRegular = PDType1Font.HELVETICA;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = 750;
                float margin = 50;
                float pageWidth = page.getMediaBox().getWidth() - 2 * margin;
                float leading = 14.5f;

                // Título
                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Comprobante de Acreditación");
                contentStream.endText();
                yPosition -= leading * 2;

                // Línea separadora
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(margin + pageWidth, yPosition);
                contentStream.stroke();
                yPosition -= leading * 1.5f;

                // Contenido
                contentStream.beginText();
                contentStream.setFont(fontRegular, 12);
                contentStream.setLeading(leading);
                contentStream.newLineAtOffset(margin, yPosition);

                contentStream.showText("Estimado/a Usuario,");
                contentStream.newLine();
                contentStream.showText("Adjuntamos el comprobante de su reciente acreditación:");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(fontBold, 12);
                contentStream.showText("ID de Acreditación: ");
                contentStream.setFont(fontRegular, 12);
                contentStream.showText(data.getAccreditationId() != null ? data.getAccreditationId().toString() : "N/A");
                contentStream.newLine();

                contentStream.setFont(fontBold, 12);
                contentStream.showText("Punto de Venta: ");
                contentStream.setFont(fontRegular, 12);
                contentStream.showText(data.getSalePointName() != null ? data.getSalePointName() : "N/A");
                contentStream.newLine();

                contentStream.setFont(fontBold, 12);
                contentStream.showText("Monto Acreditado: ");
                contentStream.setFont(fontRegular, 12);
                contentStream.showText(data.getAmount() != null ? String.format(java.util.Locale.US, "$%.2f", data.getAmount()) : "N/A");
                contentStream.newLine();

                contentStream.setFont(fontBold, 12);
                contentStream.showText("Fecha del Comprobante: ");
                contentStream.setFont(fontRegular, 12);
                contentStream.showText(data.getReceiptDate() != null ? data.getReceiptDate().format(CUSTOM_DATE_FORMATTER) : "N/A");
                contentStream.newLine();

                contentStream.setFont(fontBold, 12);
                contentStream.showText("Fecha de Creación de Acreditación: ");
                contentStream.setFont(fontRegular, 12);
                contentStream.showText(data.getCreatedAt() != null ? data.getCreatedAt().format(CUSTOM_DATE_TIME_FORMATTER) : "N/A");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.showText("Gracias por utilizar nuestros servicios.");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(fontRegular, 8);
                contentStream.showText("Generado el: " + LocalDateTime.now().format(CUSTOM_DATE_TIME_FORMATTER));
                contentStream.endText();
            }

            document.save(byteArrayOutputStream);
            log.info("PDF generado exitosamente para acreditación ID: {}", data.getAccreditationId());
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error al generar el PDF para acreditación ID {}: {}", data.getAccreditationId(), e.getMessage(), e);
            throw e;
        }
    }
}
