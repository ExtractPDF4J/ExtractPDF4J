# Table Areas

Table area selection allows you to constrain extraction to a specific region of the page.

This is useful when:

- the page contains multiple unrelated sections
- only one part of the page is tabular
- headers, footers, and notes interfere with detection
- you want to isolate a specific table

## Why table areas matter

Many real PDFs contain more than just the table:

- logos
- address blocks
- summaries
- disclaimers
- signatures
- free-form text sections

If the parser tries to interpret the full page, these can introduce noise.

Restricting extraction to a defined area helps the parser focus on the actual table.

## Concept

A table area is typically defined as a bounding box on the page.

A common format is:

```text
x1,y1,x2,y2
```

Where:

- `x1`, `y1` = top-left or starting coordinate
- `x2`, `y2` = bottom-right or ending coordinate

Exact coordinate conventions depend on parser implementation, but the goal is the same: isolate the table region.

## Example usage pattern

If your parser supports table areas, the usage may look like:

```java
new LatticeParser("invoice.pdf")
    .tableAreas("50,120,550,700")
    .parse();
```

Or multiple areas:

```java
new StreamParser("report.pdf")
    .tableAreas("50,120,550,320", "50,360,550,700")
    .parse();
```

## When to use table areas

Use table areas when:

- a page contains two tables and you want only one
- headers or footers are being mistaken for rows
- the parser detects too much page content
- the table location is stable across document templates

## Best fit scenarios

- recurring invoice templates
- fixed report layouts
- forms with known table locations
- scanned documents where full-page detection is noisy

## Example workflow

### Step 1: Inspect the page
Open the PDF and identify the visual table region.

### Step 2: Estimate coordinates
Determine the approximate bounding box for the table.

### Step 3: Constrain extraction
Pass the area into the parser.

### Step 4: Validate output
Check whether row and column accuracy improves.

## Tips

- Start with a slightly larger box, then tighten it
- Avoid clipping headers if they are needed
- Keep some margin around the visible table
- Reuse stable coordinates for repeated templates

## Common mistakes

### Area too tight
If the area cuts off the header or final column, output becomes incomplete.

### Area too broad
If the area includes unrelated text, noise may still affect results.

### Assuming all templates match
Table areas work best when document layouts are stable.

## Good practice for production

If you ingest a known family of PDFs:

- maintain template-specific coordinates
- version them when layouts change
- test against at least a few document samples before rollout

## Related pages

- [Columns](columns.md)
- [Debug Images](debug-images.md)
- [Lattice Parser](../how-it-works/lattice.md)
