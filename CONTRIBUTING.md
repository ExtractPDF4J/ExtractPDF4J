# Contributing to ExtractPDF4J

Thanks for your interest in improving ExtractPDF4J! This document keeps contribution steps short and clear.

## TL;DR
- Fork the repo and create a feature branch.
- Write clean, well-documented Java (JDK 17+).
- Add or update tests (JUnit 5). Keep PDFs **small and redacted**.
- Run the full test suite locally; ensure builds pass.
- Open a pull request with a concise description and, if relevant, attach debug overlay screenshots.

## Development Setup
- Java: 17 or 21
- Build: Maven 3.9+ (or Gradle 7+)
- Native deps (for Lattice/OCR):
  - OpenCV 4.x (via bytedeco `opencv-platform` preferred)
  - Tesseract 5.x (via bytedeco `tesseract-platform` preferred)

### Build & Test (Maven)
```bash
mvn -q -DskipTests=false clean verify
```

### Build & Test (Gradle)
```bash
./gradlew clean test
```

## Coding Guidelines
- Follow **Google Java Style** (run `google-java-format` or configure `spotless`).
- Prefer descriptive method names and **JavaDoc** for public APIs.
- Keep methods small and focused; avoid static utility sprawl.
- Handle errors explicitly; surface actionable messages.

## Tests
- Use **JUnit 5**.
- Add sample PDFs that are tiny, sanitized, and legally shareable.
- For image-based parsing, include expected CSV/JSON fixtures.

## Debug Artifacts
When changing lattice/ocr/auto detection, enable debug overlays and attach relevant images in the PR to illustrate behavior.

## Commit Messages
- Use concise messages; optionally follow **Conventional Commits**:
  - `feat:`, `fix:`, `perf:`, `docs:`, `refactor:`, `test:`, `build:`

## Issue Reports
Please include:
- OS, JDK version, build tool + version
- Parser flavor (stream/lattice/auto/hybrid) and key config
- A small sample PDF (if possible) + expected vs actual output
- Debug overlays (lines/joints/ocr boxes) if lattice/ocr is involved

## License
By contributing, you agree that your contributions will be licensed under the **Apache-2.0** License.
