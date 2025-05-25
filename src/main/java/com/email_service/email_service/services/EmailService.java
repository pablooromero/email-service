package com.email_service.email_service.services;

import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.AccreditationPdfEvent;
import com.email_service.email_service.models.EmailEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public interface EmailService {
    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    void sendRegistrationEmail(EmailEvent emailEvent);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PDF)
    void sendAccreditationPdfEmail(AccreditationPdfEvent pdfEvent);
}
