# Installation

## Maven

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-parser</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Gradle

```kotlin
implementation("io.github.extractpdf4j:extractpdf4j-parser:2.0.0")
```

## Native dependencies notes

For scanned/image-heavy PDFs, OCR/OpenCV runtime support is required.

- Recommended: Bytedeco `*-platform` artifacts.
- If using system installs, ensure native library paths are configured.
- Set `TESSDATA_PREFIX` when Tesseract language packs are not auto-detected.

See [OCR tuning](advanced-usage/ocr-tuning.md) for practical settings.
