# OCR Tuning

For scanned/image-heavy PDFs, tuning OCR settings is often the biggest quality lever.

## Recommended starting points

- DPI: `300` for normal scans, `400-450` for low-quality scans
- OCR backend: `auto` first, then force backend if needed
- Language data: verify `TESSDATA_PREFIX`

## CLI example

```bash
java -jar extractpdf4j-parser-<version>.jar scan.pdf \
  --mode ocrstream \
  --dpi 400 \
  --ocr bytedeco \
  --out out.csv
```
