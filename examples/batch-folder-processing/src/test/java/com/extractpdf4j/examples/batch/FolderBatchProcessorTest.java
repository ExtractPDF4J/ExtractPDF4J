package com.extractpdf4j.examples.batch;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FolderBatchProcessorTest {

  @Test
  void listPdfsIgnoresNonPdfFiles(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("a.pdf"), "stub");
    Files.writeString(dir.resolve("b.txt"), "text");
    Files.writeString(dir.resolve("c.PDF"), "stub");

    List<Path> pdfs = FolderBatchProcessor.listPdfs(dir);
    assertEquals(2, pdfs.size());
    assertTrue(pdfs.stream().anyMatch(p -> p.getFileName().toString().equals("a.pdf")));
    assertTrue(pdfs.stream().anyMatch(p -> p.getFileName().toString().equals("c.PDF")));
  }

  @Test
  void batchRunWritesCsvForTextPdf(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("input");
    Path output = temp.resolve("output");
    Files.createDirectories(input);
    writeSimpleTablePdf(input.resolve("invoice.pdf"));

    int count = new FolderBatchProcessor(input, output).run();
    assertEquals(1, count);
    Path csv = output.resolve("invoice.csv");
    assertTrue(Files.isRegularFile(csv));
    String content = Files.readString(csv);
    assertFalse(content.isBlank());
    assertTrue(content.contains("Amount") || content.contains("Item"));
  }

  private static void writeSimpleTablePdf(Path target) throws Exception {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage();
      document.addPage(page);
      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.newLineAtOffset(50, 700);
        content.showText("Item    Amount");
        content.newLineAtOffset(0, -20);
        content.showText("Widget  10.00");
        content.endText();
      }
      document.save(target.toFile());
    }
  }
}
