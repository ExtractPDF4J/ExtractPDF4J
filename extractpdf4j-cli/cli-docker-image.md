# Using the ExtractPDF4J Docker Image

## Prerequisites

- Docker installed and running.
- Run the build command from the repository root—the directory containing the top-level `pom.xml`.
- Create an `input-tests` folder and place the PDF you want to process in it.

## Build the image

Run this from the repository root:

```bash
docker build -f extractpdf4j-cli/Dockerfile -t extractpdf4j-cli:latest .
```

The final `.` tells Docker to use the repository root as the build context.

## Prepare input and output folders

Create these folders to save the CLI inputs and outputs. 
The chosen approach here is to create them in the project root, but you can do it any other place just make sure to have the proper permissions:

- `input-tests` — put PDFs here.
- `output-tests` — generated files will be saved here.

For example:

```text
your-project/
├── input-tests/
│   └── Sample_Utility_Bill.pdf
└── output-tests/
```

## Run the CLI

Choose the command for your terminal. Each command mounts the host folders into the container, so input files can be read and results remain available on your computer after the container exits.

### Linux or macOS

```bash
docker run --rm \
  -v "$PWD/input-tests:/app/inputs" \
  -v "$PWD/output-tests:/app/outputs" \
  extractpdf4j-cli:latest \
  /app/inputs/Sample_Utility_Bill.pdf \
  --pages all \
  --out /app/outputs/examples.txt
```

On Fedora, RHEL, or another Linux system using SELinux, add `:Z` to each volume mount:

```bash
-v "$PWD/input-tests:/app/inputs:Z"
-v "$PWD/output-tests:/app/outputs:Z"
```

If Linux reports a permission error writing to `output-tests`, make sure the directory is writable by the user running the container. For a local test, you can run `chmod 777 output-tests`; avoid this permission setting for shared or production systems.

### Windows PowerShell

Run from the repository root:

```powershell
docker run --rm `
  --mount "type=bind,source=$($PWD.Path)\input-tests,target=/app/inputs" `
  --mount "type=bind,source=$($PWD.Path)\output-tests,target=/app/outputs" `
  extractpdf4j-cli:latest `
  /app/inputs/Sample_Utility_Bill.pdf `
  --pages all `
  --out /app/outputs/examples.txt
```

### Windows Command Prompt

Run from the repository root:

```cmd
docker run --rm ^
  --mount "type=bind,source=%cd%\input-tests,target=/app/inputs" ^
  --mount "type=bind,source=%cd%\output-tests,target=/app/outputs" ^
  extractpdf4j-cli:latest ^
  /app/inputs/Sample_Utility_Bill.pdf ^
  --pages all ^
  --out /app/outputs/examples.txt
```

## Find the result

When the command finishes, look in `output-tests` on your computer. The CLI may create a filename such as `examples-1.txt` rather than `examples.txt`, depending on its output behavior.

## Troubleshooting

- **Image not found:** Build the image first. Make sure the build and run commands use the same tag: `extractpdf4j-cli:latest`.
- **Input file not found:** Confirm the PDF is inside `input-tests` and that its name and capitalization match the command.
- **Permission or access error:** Confirm Docker can access the folders. On Docker Desktop, check that the folder or drive is shared with Docker. On SELinux-enabled Linux, add `:Z` to the volume mounts.
