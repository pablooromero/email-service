package com.email_service.email_service.services.implementations;
import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.EmailEvent;
import com.email_service.email_service.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImplementation implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImplementation.class);

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void sendRegistrationEmail(EmailEvent emailEvent) {
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
}
