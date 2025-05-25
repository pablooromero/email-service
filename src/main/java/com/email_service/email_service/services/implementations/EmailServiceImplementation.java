package com.email_service.email_service.services.implementations;
import com.email_service.email_service.builders.PdfGenerator;
import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.AccreditationPdfEvent;
import com.email_service.email_service.models.EmailEvent;
import com.email_service.email_service.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImplementation implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImplementation.class);

    private final JavaMailSender mailSender;
    private final PdfGenerator pdfGenerator;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    @Override
    public void sendRegistrationEmail(EmailEvent emailEvent) {
        if (emailEvent == null || !StringUtils.hasText(emailEvent.to())) {
            log.warn("Evento de correo de registro inválido o sin destinatario: {}", emailEvent);
            return;
        }

        logger.info("Recibido evento de correo para: {}", emailEvent.to());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailEvent.to());
            message.setSubject(emailEvent.subject());
            message.setText(emailEvent.body());

            mailSender.send(message);
            logger.info("Correo enviado exitosamente a: {}", emailEvent.to());
        } catch (Exception e) {
            logger.error("Error al enviar correo a {}: {}", emailEvent.to(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PDF)
    @Override
    public void sendAccreditationPdfEmail(AccreditationPdfEvent pdfEvent) {
        if (pdfEvent == null || !StringUtils.hasText(pdfEvent.getTo()) || pdfEvent.getAccreditationData() == null) {
            logger.warn("Evento de PDF inválido o sin datos suficientes: {}", pdfEvent);
            return;
        }
        logger.info("Recibido evento de PDF para acreditación ID: {} para el correo: {}",
                pdfEvent.getAccreditationData().getAccreditationId(), pdfEvent.getTo());

        try {
            byte[] pdfBytes = pdfGenerator.generateAccreditationPdf(pdfEvent.getAccreditationData());

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(pdfEvent.getTo());
            helper.setSubject(pdfEvent.getSubject() != null ? pdfEvent.getSubject() : "Comprobante de Acreditación");
            helper.setText(pdfEvent.getBodyHeader() != null ? pdfEvent.getBodyHeader() : "Estimado/a Usuario,\n\nAdjuntamos su comprobante de acreditación.\n\nSaludos");

            String attachmentFilename = "ComprobanteAcreditacion-" + pdfEvent.getAccreditationData().getAccreditationId() + ".pdf";
            helper.addAttachment(attachmentFilename, new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(mimeMessage);
            logger.info("Correo con PDF de acreditación ID {} enviado exitosamente a: {}",
                    pdfEvent.getAccreditationData().getAccreditationId(), pdfEvent.getTo());

        } catch (MessagingException e) {
            logger.error("Error de mensajería al enviar correo con PDF para acreditación ID {} a {}: {}",
                    pdfEvent.getAccreditationData().getAccreditationId(), pdfEvent.getTo(), e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Error de IO al generar PDF para acreditación ID {} para {}: {}",
                    pdfEvent.getAccreditationData().getAccreditationId(), pdfEvent.getTo(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado al procesar evento de PDF para acreditación ID {} a {}: {}",
                    pdfEvent.getAccreditationData().getAccreditationId(), pdfEvent.getTo(), e.getMessage(), e);
        }
    }
}
