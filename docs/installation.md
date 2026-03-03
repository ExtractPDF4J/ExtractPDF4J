# Installation

## Java and build requirements

- Java 17+
- Maven or Gradle
- OCR/OpenCV runtime support for scanned/image-heavy workflows

## Maven coordinates

### Parser (primary dependency)

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-parser</artifactId>
  <version>2.0.0</version>
</dependency>
```

### Other published modules

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-core</artifactId>
  <version>2.0.0</version>
</dependency>
```

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-cli</artifactId>
  <version>2.0.0</version>
</dependency>
```

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-service</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Gradle

```kotlin
implementation("io.github.extractpdf4j:extractpdf4j-parser:2.0.0")
```

## Native/OCR notes

- Recommended approach: use Bytedeco `*-platform` artifacts where possible.
- If using system-level installs, ensure native library paths are configured for your OS (`LD_LIBRARY_PATH`, `DYLD_LIBRARY_PATH`, or Windows `PATH`).
- Set `TESSDATA_PREFIX` if OCR language data is not auto-discovered.

See [OCR tuning](advanced-usage/ocr-tuning.md) for practical defaults.
