# Documentation Development

This page explains how to work on the MkDocs site locally and how documentation is built in CI.

## Where docs live

- Source pages: [`/docs`](./)
- Site config: [`mkdocs.yml`](../mkdocs.yml)
- Deployment workflow: [`.github/workflows/docs-deploy.yml`](../.github/workflows/docs-deploy.yml)

## Local prerequisites

- Python 3.9+
- `pip`

Install MkDocs Material:

```bash
pip install mkdocs-material
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

Docs are built in GitHub Actions using the Material-maintained action:

```yaml
- name: Build docs with Material action (bundled MkDocs)
  uses: squidfunk/mkdocs-material@v9
  with:
    command: build --strict
```

Why `@v9`?

- It tracks stable v9 patch updates.
- It avoids hard-pinning to a patch ref that may not exist.
- It keeps CI behavior aligned with the local `mkdocs build --strict` command documented in the README.
