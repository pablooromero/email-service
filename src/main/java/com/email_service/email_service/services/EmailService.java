package com.email_service.email_service.services;

import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.EmailEvent;
import com.email_service.email_service.models.OrderToPdfDTO;
import com.email_service.email_service.models.ProductRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
    public void sendPdfOrderEmail (OrderToPdfDTO orderDTO){
        System.out.println("se creo el pdf");
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        try {
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            generatePdfContent(contentStream, orderDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            document.save("document.pdf");
            document.close();
            System.out.println("se creo el pdf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generatePdfContent(PDPageContentStream contentStream, OrderToPdfDTO orderDTO) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("Order ID: "+orderDTO.getOrderId());
        contentStream.newLine();
        for (ProductRecord item : orderDTO.getnewProductList()){
            contentStream.showText("Product ID: " + item.id() + " name: " + item.name() + " description: " + item.description()+" price: "+ item.price()+  " Quantity: "+ item.quantity());
            contentStream.newLine();
        }
        contentStream.endText();
        contentStream.close();
    }
}
