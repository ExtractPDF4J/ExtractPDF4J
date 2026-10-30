package com.extractpdf4j.examples.batch;

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Processes every {@code .pdf} file in an input directory and writes the first extracted table
 * from each file to CSV under an output directory (one CSV per PDF).
 */
public final class FolderBatchProcessor {

  private final Path inputDir;
  private final Path outputDir;

  public FolderBatchProcessor(Path inputDir, Path outputDir) {
    this.inputDir = inputDir;
    this.outputDir = outputDir;
  }

  /**
   * @return number of PDF files processed
   */
  public int run() throws IOException {
    Files.createDirectories(outputDir);
    List<Path> pdfs = listPdfs(inputDir);
    if (pdfs.isEmpty()) {
      System.err.println("No PDF files found in: " + inputDir.toAbsolutePath());
      return 0;
    }

    int processed = 0;
    for (Path pdf : pdfs) {
      processed += processOne(pdf);
    }
    System.out.printf(
        Locale.ROOT,
        "Processed %d PDF(s); CSV output in %s%n",
        processed,
        outputDir.toAbsolutePath());
    return processed;
  }

  static List<Path> listPdfs(Path directory) throws IOException {
    if (!Files.isDirectory(directory)) {
      throw new IOException("Input path is not a directory: " + directory);
    }
    List<Path> pdfs = new ArrayList<>();
    try (Stream<Path> stream = Files.list(directory)) {
      stream
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf"))
          .sorted(Comparator.comparing(p -> p.getFileName().toString()))
          .forEach(pdfs::add);
    }
    return pdfs;
  }

  private int processOne(Path pdf) throws IOException {
    String fileName = pdf.getFileName().toString();
    System.out.println("Extracting: " + fileName);
    List<Table> tables = new HybridParser(pdf.toString()).dpi(300f).parse();
    if (tables.isEmpty()) {
      System.out.println("  No tables found.");
      return 1;
    }
    Path csv = outputDir.resolve(stripExtension(fileName) + ".csv");
    Files.writeString(csv, tables.get(0).toCSV(','));
    System.out.printf(Locale.ROOT, "  Wrote %s (%d table(s) total)%n", csv.getFileName(), tables.size());
    return 1;
  }

  private static String stripExtension(String name) {
    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
  }

  public static void main(String[] args) throws Exception {
    Path input = Path.of(args.length > 0 ? args[0] : "input");
    Path output = Path.of(args.length > 1 ? args[1] : "output");
    int code = new FolderBatchProcessor(input, output).run() > 0 ? 0 : 1;
    if (code != 0) {
      System.exit(code);
    }
  }
}
