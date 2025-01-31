package com.email_service.email_service.services;

import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.EmailEvent;
import com.email_service.email_service.models.OrderToPdfDTO;
import com.email_service.email_service.models.ProductRecord;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void sendRegistrationEmail(EmailEvent emailEvent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailEvent.getTo());
        message.setSubject(emailEvent.getSubject());
        message.setText(emailEvent.getBody());
        mailSender.send(message);
        System.out.println("Correo enviado a: " + emailEvent.getTo());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PDF)
    public void sendPdfOrderEmail (OrderToPdfDTO orderDTO) throws MessagingException {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            generatePdfContent(contentStream, orderDTO, page);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();
            document.close();
            System.out.println("se creo el pdf");

            System.out.println(orderDTO.getUserMail());
            sendEmail(orderDTO.getUserMail(),pdfBytes,"document.pdf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generatePdfContent(PDPageContentStream contentStream, OrderToPdfDTO orderDTO, PDPage page) throws IOException {
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
        contentStream.close();
    }

    private void sendEmail(String to, byte[] pdfBytes, String fileName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Your Order Confirmation");
        helper.setText("Dear Customer,\n\nPlease find your order details attached.", false);

        helper.addAttachment(fileName, () -> new java.io.ByteArrayInputStream(pdfBytes));

        mailSender.send(message);
    }
}
