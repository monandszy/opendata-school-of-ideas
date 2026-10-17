# Root Justfile for OpenData Warsaw project

set windows-shell := ["pwsh.exe", "-NoProfile", "-Command"]

# OpenAPI specification generator for Warsaw Open Data
mod api-gen

# Kotlin Multiplatform application (Android, Web)
mod app

# Default recipe: show help / recipes across all modules
default:
    @just --list --list-submodules
