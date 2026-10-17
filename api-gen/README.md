# API Specification Generator (`api-gen`)

The `api-gen` module is a Python tool that scrapes public API metadata from the [Warsaw Open Data Portal](https://dane.um.warszawa.pl), parses request formats and parameter definitions, and automatically outputs OpenAPI 3.0 specification files (`openapi.yaml` and `openapi.json`) into the [`../docs/`](../docs/) directory.

---

## 📁 Module Structure

- **`generate_openapi.py`**: Core script responsible for downloading endpoint documentation, parsing cURL samples, converting Polish field types to OpenAPI schemas, and assembling the spec.
- **`cache/`**: Disk cache directory used to store raw metadata JSON responses to avoid redundant network requests during generation.

---