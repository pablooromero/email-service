package com.email_service.email_service.builders;

import com.email_service.email_service.models.AccreditationDataForPdf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PdfGeneratorTest {

    private PdfGenerator pdfGenerator;

    private AccreditationDataForPdf validPdfData;

    @BeforeEach
    void setUp() {
        pdfGenerator = new PdfGenerator();
        validPdfData = new AccreditationDataForPdf(
                1L, "Punto de Venta Central", 200L, "usuario@ejemplo.com",
                123.45, LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("generateAccreditationPdf - Debería generar PDF con datos válidos sin lanzar excepción")
    void generateAccreditationPdf_withValidData_shouldGeneratePdfBytes() {
        assertDoesNotThrow(() -> {
            byte[] pdfBytes = pdfGenerator.generateAccreditationPdf(validPdfData);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0, "El array de bytes del PDF no debería estar vacío");

            String pdfAsString = new String(pdfBytes, 0, Math.min(pdfBytes.length, 8));
            assertTrue(pdfAsString.startsWith("%PDF-"), "Debería empezar con el header de PDF");
        }, "La generación del PDF no debería lanzar IOException con datos válidos.");
    }

    @Test
    @DisplayName("generateAccreditationPdf - Debería manejar datos parcialmente nulos en AccreditationDataForPdf")
    void generateAccreditationPdf_withPartialNullData_shouldStillGenerate() {
        AccreditationDataForPdf partialData = new AccreditationDataForPdf();
        partialData.setAccreditationId(2L);
        partialData.setUserEmail("test@example.com");

        assertDoesNotThrow(() -> {
            byte[] pdfBytes = pdfGenerator.generateAccreditationPdf(partialData);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);
        }, "La generación del PDF debería manejar campos nulos en los datos de entrada.");
    }

    @Test
    @DisplayName("generateAccreditationPdf - Debería lanzar IllegalArgumentException si los datos son completamente nulos")
    void generateAccreditationPdf_withNullData_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pdfGenerator.generateAccreditationPdf(null);
        });

        assertEquals("Accreditation data cannot be null for PDF generation.", exception.getMessage());
    }

    @Test
    @DisplayName("generateAccreditationPdf - Debería generar un PDF con todos los campos de datos nulos (excepto ID para el log)")
    void generateAccreditationPdf_withAllDataFieldsNull_shouldUseNA() {
        AccreditationDataForPdf allNullData = new AccreditationDataForPdf();
        allNullData.setAccreditationId(3L);

        assertDoesNotThrow(() -> {
            byte[] pdfBytes = pdfGenerator.generateAccreditationPdf(allNullData);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);
        }, "La generación de PDF con campos de datos nulos debería usar 'N/A' y no fallar.");
    }
}