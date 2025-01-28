package com.email_service.email_service.services;

import com.email_service.email_service.config.RabbitMQConfig;
import com.email_service.email_service.models.EmailEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void sendEmail(EmailEvent emailEvent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailEvent.getTo());
        message.setSubject(emailEvent.getSubject());
        message.setText(emailEvent.getBody());
        mailSender.send(message);

        System.out.println("Correo enviado a: " + emailEvent.getTo());
    }
}
