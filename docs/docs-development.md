# Documentation Development

This page explains how to work on the MkDocs site locally and how documentation is built in CI.

## Where docs live

- Source pages: [`/docs`](index.md)
- Site config: [`mkdocs.yml`](https://github.com/ExtractPDF4J/ExtractPDF4J/blob/main/mkdocs.yml)
- Deployment workflow: [`.github/workflows/docs-deploy.yml`](https://github.com/ExtractPDF4J/ExtractPDF4J/blob/main/.github/workflows/docs-deploy.yml)

## Local prerequisites

- Python 3.9+
- `pip`

Install MkDocs Material:

```bash
pip install -r docs/requirements.txt
```

## Build docs locally

Run a strict docs build (same behavior used in CI):

```bash
mkdocs build --strict
```

## Serve docs locally

Start a local preview server:

```bash
mkdocs serve
```

Default URL:

- http://127.0.0.1:8000

## CI behavior (GitHub Actions)

Docs are built in GitHub Actions with Python + pinned docs dependencies:

```yaml
- name: Set up Python
  uses: actions/setup-python@v5
  with:
    python-version: '3.11'

- name: Install docs dependencies
  run: pip install -r docs/requirements.txt

- name: Build docs
  run: mkdocs build --strict
```

Why pinned requirements?

- They keep local and CI behavior aligned.
- They avoid pulling MkDocs 2.0 while Material for MkDocs reports incompatibility warnings.
- They preserve strict docs builds with predictable versions.
