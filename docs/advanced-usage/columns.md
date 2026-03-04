# Columns

Column guidance helps improve extraction when column boundaries are ambiguous.

This is especially useful for:

- borderless tables
- loosely aligned text layouts
- wrapped rows
- documents with inconsistent spacing

## Why column guidance matters

In many real-world PDFs, columns are not explicitly drawn.

Instead, they are implied by:

- text alignment
- spacing
- recurring positions
- visual grouping

When alignment is imperfect, the parser may:

- merge columns
- split one column into two
- shift values into neighboring columns

Providing column hints can improve consistency.

## Common use cases

Use column guidance when:

- dates, descriptions, and amounts are drifting
- the parser merges narrow columns
- statement rows are inconsistently aligned
- one template repeats across many files

## Example usage pattern

If your parser supports explicit column hints, usage may look like:

```java
new StreamParser("statement.pdf")
    .columns("80,180,420,520")
    .parse();
```

This typically tells the parser where column boundaries should be inferred.

## Typical example

Imagine a statement layout with:

- Date
- Description
- Debit
- Credit
- Balance

If spacing is inconsistent, explicitly guiding column positions can help preserve the intended structure.

## Benefits

- more stable column alignment
- fewer accidental merges
- better CSV consistency
- easier downstream normalization

## Best fit scenarios

- text-based bank statements
- invoice line items
- reports with repeated narrow numeric columns
- PDFs where visible lines do not exist

## When not to rely on columns alone

Column hints are not a replacement for:

- OCR when text is missing
- lattice parsing when borders clearly define the table
- table areas when full-page noise is the main problem

Use them as a focused improvement for ambiguous text layouts.

## Workflow recommendation

### Start simple
Try extraction without hints first.

### Add column guidance only if needed
If output is unstable, introduce column hints.

### Validate with multiple samples
Do not tune against a single document only.

## Common mistakes

### Overfitting to one sample
A perfect fit for one PDF may fail on a slightly shifted template.

### Using columns when the issue is actually OCR quality
If the page is scanned, improve OCR or DPI first.

### Ignoring headers
Make sure the hinted boundaries still align with the header row.

## Related pages

- [Page Ranges](page-ranges.md)
- [Table Areas](table-areas.md)
- [Stream Parser](../how-it-works/stream.md)
