package com.email_service.email_service.builders;

import com.email_service.email_service.models.OrderToPdfDTO;
import com.email_service.email_service.models.ProductRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class PdfGenerator {
    public byte[] generateOrderPdf(OrderToPdfDTO orderDTO) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            generatePdfContent(contentStream, orderDTO);
            contentStream.close();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }


    private void generatePdfContent(PDPageContentStream contentStream, OrderToPdfDTO orderDTO) throws IOException {
        PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
        PDType1Font textFont = PDType1Font.HELVETICA;

        contentStream.setFont(titleFont, 16);
        contentStream.beginText();
        contentStream.setLeading(20f);
        contentStream.newLineAtOffset(220, 750);
        contentStream.showText("Order Confirmation");
        contentStream.endText();

        contentStream.setFont(textFont, 12);
        contentStream.beginText();
        contentStream.setLeading(16f);
        contentStream.newLineAtOffset(64, 700);

        contentStream.showText("--------------------------------------------------------------------------------------------------------------------------");
        contentStream.newLine();
        contentStream.showText("ID       Name                     Description                Price      Qty");
        contentStream.newLine();
        contentStream.showText("--------------------------------------------------------------------------------------------------------------------------");
        contentStream.newLine();

        for (ProductRecord item : orderDTO.getnewProductList()) {
            String productLine = String.format("%-8s %-20s %-25s %-8s %-4s",
                    item.id(), item.name(), item.description(), item.price(), item.quantity());
            contentStream.showText(productLine);
            contentStream.newLine();
        }

        contentStream.showText("--------------------------------------------------------------------------------------------------------------------------");
        contentStream.endText();
    }
}
