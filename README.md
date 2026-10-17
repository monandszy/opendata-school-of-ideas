# OpenData Warsaw

Repository for working with **Warsaw Open Data** (`dane.um.warszawa.pl`).

The goal was to create a map of Warsaw showing the heat distribution.

---

## Submodules Linktree

- [**`api-gen/`**](./api-gen/README.md) – Python utility for fetching metadata from `dane.um.warszawa.pl` and compiling OpenAPI 3.0 specifications (`openapi.yaml` / `openapi.json`).
- [**`app/`**](./app/README.md) – Kotlin Multiplatform & Compose Multiplatform app targeting Android and Web (Kotlin/Wasm), featuring spatial IDW heatmaps and interactive map overlays.
- [**`docs/`**](./docs/ApiList.md) – API reference documentation, list of endpoints, and pre-compiled OpenAPI specifications (`openapi.yaml`, `openapi.json`).

---

## Quick Start

This repository uses [`just`](https://github.com/casey/just) as a central task runner for cross-module development tasks. Run it to access further documentation.