package com.email_service.email_service.services;

import com.email_service.email_service.builders.PdfGenerator;
import com.email_service.email_service.models.AccreditationDataForPdf;
import com.email_service.email_service.models.AccreditationPdfEvent;
import com.email_service.email_service.models.EmailEvent;
import com.email_service.email_service.services.implementations.EmailServiceImplementation;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplementationTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PdfGenerator pdfGenerator;

    @InjectMocks
    private EmailServiceImplementation emailService;

    private EmailEvent sampleEmailEvent;
    private AccreditationPdfEvent samplePdfEvent;
    private AccreditationDataForPdf sampleAccreditationData;

    @BeforeEach
    void setUp() {
        sampleEmailEvent = new EmailEvent("test@example.com", "Asunto de Prueba", "Cuerpo del correo de prueba.");

        sampleAccreditationData = new AccreditationDataForPdf(
                1L, "Punto de Venta X", 101L, "user@example.com",
                250.0, LocalDateTime.now(), LocalDateTime.now()
        );
        samplePdfEvent = new AccreditationPdfEvent(
                "user@example.com",
                "Comprobante Acreditación #1",
                "Adjunto comprobante.",
                sampleAccreditationData
        );
    }


    @Test
    @DisplayName("sendRegistrationEmail - Debería enviar SimpleMailMessage con datos correctos")
    void sendRegistrationEmail_withValidEvent_shouldSendSimpleMail() {
        emailService.sendRegistrationEmail(sampleEmailEvent);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage.getTo(), "El destinatario no debería ser null");
        assertEquals(1, capturedMessage.getTo().length, "Debería haber un destinatario");
        assertEquals(sampleEmailEvent.to(), capturedMessage.getTo()[0]);
        assertEquals(sampleEmailEvent.subject(), capturedMessage.getSubject());
        assertEquals(sampleEmailEvent.body(), capturedMessage.getText());
    }

    @Test
    @DisplayName("sendRegistrationEmail - Debería loguear error si mailSender lanza excepción")
    void sendRegistrationEmail_whenMailSenderThrowsException_shouldLogError() {
        doThrow(new MailSendException("Error simulado de envío"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendRegistrationEmail(sampleEmailEvent);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendRegistrationEmail - No debería hacer nada si el evento es null")
    void sendRegistrationEmail_withNullEvent_shouldDoNothing() {
        emailService.sendRegistrationEmail(null);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendRegistrationEmail - No debería hacer nada si el destinatario es null o vacío")
    void sendRegistrationEmail_withNullOrEmptyTo_shouldDoNothing() {
        EmailEvent eventWithNullTo = new EmailEvent(null, "Sujeto", "Cuerpo");
        EmailEvent eventWithEmptyTo = new EmailEvent("", "Sujeto", "Cuerpo");
        EmailEvent eventWithBlankTo = new EmailEvent("   ", "Sujeto", "Cuerpo");

        emailService.sendRegistrationEmail(eventWithNullTo);
        emailService.sendRegistrationEmail(eventWithEmptyTo);
        emailService.sendRegistrationEmail(eventWithBlankTo);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }


    @Test
    @DisplayName("sendAccreditationPdfEmail - Debería generar PDF y enviar MimeMessage con adjunto")
    void sendAccreditationPdfEmail_withValidEvent_shouldGeneratePdfAndSendMimeMessage() throws IOException, MessagingException {
        byte[] fakePdfBytes = "fake-pdf-content".getBytes();
        when(pdfGenerator.generateAccreditationPdf(eq(sampleAccreditationData)))
                .thenReturn(fakePdfBytes);

        MimeMessage mockMimeMessage = spy(new MimeMessage((Session) null));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        emailService.sendAccreditationPdfEmail(samplePdfEvent);

        verify(pdfGenerator, times(1)).generateAccreditationPdf(eq(sampleAccreditationData));
        verify(mailSender, times(1)).createMimeMessage();

        ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(mimeMessageCaptor.capture());

        MimeMessage capturedMimeMessage = mimeMessageCaptor.getValue();

        assertNotNull(capturedMimeMessage.getRecipients(MimeMessage.RecipientType.TO), "Los destinatarios no deberían ser null");
        assertEquals(samplePdfEvent.getTo(), capturedMimeMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertEquals(samplePdfEvent.getSubject(), capturedMimeMessage.getSubject());
    }

    @Test
    @DisplayName("sendAccreditationPdfEmail - No debería hacer nada si el evento es null o datos incompletos")
    void sendAccreditationPdfEmail_withInvalidEvent_shouldDoNothing() throws IOException {
        emailService.sendAccreditationPdfEmail(null);

        AccreditationPdfEvent eventNoTo = new AccreditationPdfEvent(null, "S", "B", sampleAccreditationData);
        emailService.sendAccreditationPdfEmail(eventNoTo);

        AccreditationPdfEvent eventEmptyTo = new AccreditationPdfEvent("", "S", "B", sampleAccreditationData);
        emailService.sendAccreditationPdfEmail(eventEmptyTo);

        AccreditationPdfEvent eventBlankTo = new AccreditationPdfEvent("   ", "S", "B", sampleAccreditationData);
        emailService.sendAccreditationPdfEmail(eventBlankTo);

        AccreditationPdfEvent eventNoData = new AccreditationPdfEvent("test@test.com", "S", "B", null);
        emailService.sendAccreditationPdfEmail(eventNoData);

        verify(pdfGenerator, never()).generateAccreditationPdf(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendAccreditationPdfEmail - Debería loguear error si PdfGenerator lanza IOException")
    void sendAccreditationPdfEmail_whenPdfGeneratorThrowsIOException_shouldLogError() throws IOException {
        when(pdfGenerator.generateAccreditationPdf(eq(sampleAccreditationData))) // Usar eq()
                .thenThrow(new IOException("Error simulado de generación de PDF"));

        emailService.sendAccreditationPdfEmail(samplePdfEvent);

        verify(pdfGenerator, times(1)).generateAccreditationPdf(eq(sampleAccreditationData));
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendAccreditationPdfEmail - Debería loguear error si mailSender lanza MessagingException")
    void sendAccreditationPdfEmail_whenMailSenderThrowsMessagingException_shouldLogError() throws IOException, MessagingException {
        byte[] fakePdfBytes = "fake-pdf-content".getBytes();
        when(pdfGenerator.generateAccreditationPdf(eq(sampleAccreditationData)))
                .thenReturn(fakePdfBytes);
        MimeMessage mockMimeMessage = spy(new MimeMessage((Session) null));
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        doThrow(new MailSendException("Error simulado de envío con adjunto"))
                .when(mailSender).send(eq(mockMimeMessage));

        emailService.sendAccreditationPdfEmail(samplePdfEvent);

        verify(mailSender, times(1)).send(eq(mockMimeMessage));
    }
}
