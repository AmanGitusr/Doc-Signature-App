package com.docsignature.backend.service;

import com.docsignature.backend.domain.SignatureRequestEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfSigningService {
    public Path stamp(Path inputFile, Path outputFile, SignatureRequestEntity signatureRequest) throws IOException {
        try (PDDocument document = PDDocument.load(inputFile.toFile())) {
            int index = Math.max(0, Math.min(signatureRequest.getPageNumber() - 1, document.getNumberOfPages() - 1));
            PDPage page = document.getPage(index);
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float x = (float) (signatureRequest.getXPosition() / 100.0d * pageWidth);
            float y = (float) (pageHeight - (signatureRequest.getYPosition() / 100.0d * pageHeight));

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.setNonStrokingColor(20, 20, 20);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(x, y);
                contentStream.showText("Signed by: " + signatureRequest.getSignerName() + " <" + signatureRequest.getSignerEmail() + ">");
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Signature: " + safe(signatureRequest.getSignatureText()));
                contentStream.newLineAtOffset(0, -14);
                contentStream.showText("Status: " + signatureRequest.getStatus().name());
                contentStream.endText();
            }

            Files.createDirectories(outputFile.getParent());
            document.save(outputFile.toFile());
            return outputFile;
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Accepted electronically" : value;
    }
}
