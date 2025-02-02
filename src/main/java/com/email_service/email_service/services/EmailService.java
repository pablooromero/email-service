package com.email_service.email_service.services;

import com.email_service.email_service.builders.PdfGenerator;
import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.EmailEvent;
import com.email_service.email_service.models.OrderToPdfDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfGenerator pdfGenerator;

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
    public void sendPdfOrderEmail(OrderToPdfDTO orderDTO) throws MessagingException {
        try {
            byte[] pdfBytes = pdfGenerator.generateOrderPdf(orderDTO);
            System.out.println("Se creó el PDF correctamente.");

            System.out.println("Enviando PDF al correo: " + orderDTO.getUserMail());
            sendEmail(orderDTO.getUserMail(), pdfBytes, "document.pdf");
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
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
