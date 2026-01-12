package com.example.demo.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Controller
@RequestMapping("/api")
public class PDFController {

    @GetMapping("/pdf/invitacion/{invitadoId}")
    public ResponseEntity<byte[]> generarPDFInvitacion(@PathVariable Long invitadoId) {
        try {
            // Leer el HTML de la plantilla
            ClassPathResource resource = new ClassPathResource("static/invitacion-print.html");
            String htmlContent = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);

            // Generar PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "file:///");
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Invitacion-Boda.pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
