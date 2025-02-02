package com.email_service.email_service.services;

import com.email_service.email_service.builders.PdfGenerator;
import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.EmailEvent;
import com.email_service.email_service.models.OrderToPdfDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfGenerator pdfGenerator;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void sendRegistrationEmail(EmailEvent emailEvent) {
        logger.info("Recibido evento de correo para: {}", emailEvent.getTo());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailEvent.getTo());
            message.setSubject(emailEvent.getSubject());
            message.setText(emailEvent.getBody());

            mailSender.send(message);
            logger.info("Correo enviado exitosamente a: {}", emailEvent.getTo());
        } catch (Exception e) {
            logger.error("Error al enviar correo a {}: {}", emailEvent.getTo(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PDF)
    public void sendPdfOrderEmail(OrderToPdfDTO orderDTO) throws MessagingException {
        logger.info("Recibido evento para generar PDF de orden para el usuario: {}", orderDTO.getUserMail());

        try {
            byte[] pdfBytes = pdfGenerator.generateOrderPdf(orderDTO);
            logger.info("PDF generado correctamente para la orden ID: {}", orderDTO.getOrderId());

            sendEmail(orderDTO.getUserMail(), pdfBytes, "document.pdf");

            logger.info("Correo con PDF enviado exitosamente a: {}", orderDTO.getUserMail());
        } catch (IOException e) {
            logger.error("Error al generar el PDF para la orden ID {}: {}", orderDTO.getOrderId(), e.getMessage(), e);
        } catch (MessagingException e) {
            logger.error("Error al enviar el correo con PDF a {}: {}", orderDTO.getUserMail(), e.getMessage(), e);
            throw e;
        }
    }


    private void sendEmail(String to, byte[] pdfBytes, String fileName) throws MessagingException {
        logger.info("Preparando correo con archivo adjunto para: {}", to);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setTo(to);
            helper.setSubject("Your Order Confirmation");
            helper.setText("Dear Customer,\n\nPlease find your order details attached.", false);

            helper.addAttachment(fileName, () -> new java.io.ByteArrayInputStream(pdfBytes));

            mailSender.send(message);
            logger.info("Correo con adjunto enviado exitosamente a: {}", to);
        } catch (MessagingException e) {
            logger.error("Error al enviar correo con adjunto a {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }
}
