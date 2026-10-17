#!/usr/bin/env python3
import os
import sys
import json
import re
import urllib.request
import yaml
from pathlib import Path

# Ensure UTF-8 standard output handling
sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "https://dane.um.warszawa.pl"
CACHE_DIR = Path(__file__).parent / "cache"
DOCS_DIR = Path(__file__).parent.parent / "docs"

CACHE_DIR.mkdir(parents=True, exist_ok=True)
DOCS_DIR.mkdir(parents=True, exist_ok=True)


def fetch_url(url: str, use_cache: bool = True) -> str:
    """Fetch URL with disk caching."""
    slug = re.sub(r'[^a-zA-Z0-9_-]', '_', url)
    cache_file = CACHE_DIR / f"{slug}.json"

    if use_cache and cache_file.exists():
        return cache_file.read_text(encoding='utf-8-sig')

    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req) as resp:
        content = resp.read().decode('utf-8-sig')
        cache_file.write_text(content, encoding='utf-8')
        return content


def map_type_to_openapi(type_str: str) -> dict:
    """Map Polish parameter/field type strings to OpenAPI schema types."""
    if not type_str:
        return {"type": "string"}

    t = type_str.strip().lower()
    if t in ["string", "tekst", "str", "token"]:
        return {"type": "string"}
    elif t in ["int", "integer", "liczba całkowita", "liczba calkowita", "number"]:
        return {"type": "integer"}
    elif t in ["float", "double", "decimal", "liczba zmiennoprzecinkowa"]:
        return {"type": "number"}
    elif t in ["boolean", "bool", "prawda/fałsz", "prawda/falsz"]:
        return {"type": "boolean"}
    elif t in ["tablica", "array", "list"]:
        return {"type": "array", "items": {"type": "object"}}
    elif t in ["obiekt", "object", "dict"]:
        return {"type": "object"}
    else:
        return {"type": "string"}


def parse_curl_command(curl_str: str) -> dict:
    """Extract HTTP method, path, headers, and request body payload from cURL string."""
    method = "POST" if "--request POST" in curl_str or "--data" in curl_str or "-d " in curl_str else "GET"
    
    # Extract URL path
    url_match = re.search(r"https?://[^'\"\s]+", curl_str)
    path = "/api/action/unknown"
    if url_match:
        full_url = url_match.group(0)
        # Extract path relative to domain
        path_match = re.search(r"/api/action/[a-zA-Z0-9_-]+", full_url)
        if path_match:
            path = path_match.group(0)

    # Extract sample request body payload if present
    data_match = re.search(r"--data\s+'([^']+)'", curl_str) or re.search(r"-d\s+'([^']+)'", curl_str)
    sample_body = None
    if data_match:
        try:
            sample_body = json.loads(data_match.group(1))
        except Exception:
            sample_body = None

    return {
        "method": method.lower(),
        "path": path,
        "sample_body": sample_body
    }


def build_schema_from_json_sample(sample_obj) -> dict:
    """Recursively infer OpenAPI schema from a sample Python object (dict, list, primitive)."""
    if sample_obj is None:
        return {"type": "string", "nullable": True}
    elif isinstance(sample_obj, bool):
        return {"type": "boolean"}
    elif isinstance(sample_obj, int):
        return {"type": "integer"}
    elif isinstance(sample_obj, float):
        return {"type": "number"}
    elif isinstance(sample_obj, str):
        return {"type": "string"}
    elif isinstance(sample_obj, list):
        if len(sample_obj) > 0:
            item_schema = build_schema_from_json_sample(sample_obj[0])
        else:
            item_schema = {"type": "object"}
        return {"type": "array", "items": item_schema}
    elif isinstance(sample_obj, dict):
        properties = {}
        for k, v in sample_obj.items():
            properties[str(k)] = build_schema_from_json_sample(v)
        return {"type": "object", "properties": properties}
    else:
        return {"type": "string"}


def build_schema_from_pa(elements: list) -> dict:
    """Build OpenAPI schema from _pa.json response parameter definitions."""
    properties = {}
    for el in elements:
        field_name = el.get("name")
        if not field_name:
            continue
        field_type = el.get("type", "String")
        field_desc = el.get("description", "")
        
        schema = map_type_to_openapi(field_type)
        if field_desc:
            schema["description"] = field_desc

        nested = el.get("nested_elements")
        if nested and isinstance(nested, list):
            if schema.get("type") == "array":
                nested_schema = build_schema_from_pa(nested)
                schema["items"] = nested_schema
            elif schema.get("type") == "object":
                nested_schema = build_schema_from_pa(nested)
                schema["properties"] = nested_schema.get("properties", {})

        properties[field_name] = schema

    return {"type": "object", "properties": properties}


def to_camel_case(text: str) -> str:
    """Convert slug or snake_case string to CamelCase."""
    clean = re.sub(r'[^a-zA-Z0-9_]', ' ', text)
    words = clean.replace('_', ' ').split()
    return ''.join(w.capitalize() for w in words)


def main():
    print("=== Warsaw Open Data OpenAPI Spec Generator ===")
    
    # 1. Fetch catalog
    print("[1/4] Fetching dataset catalog from dane.um.warszawa.pl...")
    catalog_raw = fetch_url(f"{BASE_URL}/api/datasets?size=100")
    catalog = json.loads(catalog_raw)
    datasets = catalog.get('result', {}).get('results', [])
    print(f"      Found {len(datasets)} datasets.")

    # Base OpenAPI 3.1.0 document
    openapi_doc = {
        "openapi": "3.1.0",
        "info": {
            "title": "Warsaw Open Data API (Otwarte Dane m.st. Warszaw)",
            "version": "1.0.0",
            "description": "Official OpenAPI 3.1.0 specification auto-generated from dane.um.warszawa.pl metadata catalog.",
            "contact": {
                "name": "Urząd m.st. Warszawy",
                "url": "https://dane.um.warszawa.pl"
            },
            "license": {
                "name": "Creative Commons Attribution",
                "url": "https://creativecommons.org/licenses/by/4.0/"
            }
        },
        "servers": [
            {"url": "https://dane.um.warszawa.pl", "description": "Main Portal API Gateway"},
            {"url": "https://api.um.warszawa.pl", "description": "Legacy / Real-time API Gateway"}
        ],
        "paths": {},
        "components": {
            "securitySchemes": {
                "ApiKeyAuth": {
                    "type": "apiKey",
                    "in": "query",
                    "name": "apikey",
                    "description": "API Key obtained from registration on api.um.warszawa.pl"
                },
                "BearerTokenAuth": {
                    "type": "apiKey",
                    "in": "header",
                    "name": "Authorization",
                    "description": "User token passed in Authorization header"
                }
            },
            "schemas": {}
        },
        "security": [
            {"BearerTokenAuth": []},
            {"ApiKeyAuth": []}
        ]
    }

    # 2. Process each dataset
    print("[2/4] Processing dataset metadata and parameter specifications...")
    processed_count = 0
    for ds in datasets:
        ds_id = ds['id']
        ds_name = ds['name']
        ds_title = ds['title']
        ds_notes = ds.get('notes', '')
        ds_category = ds.get('category', 'General')
        
        detail_raw = fetch_url(f"{BASE_URL}/api/datasets/{ds_id}")
        detail = json.loads(detail_raw)
        
        pa_url = detail.get('parameters')
        pw_url = detail.get('call_examples')
        
        pa_data = None
        if pa_url:
            try:
                pa_data = json.loads(fetch_url(pa_url))
            except Exception as e:
                pass
                
        pw_data = None
        if pw_url:
            try:
                pw_data = json.loads(fetch_url(pw_url))
            except Exception as e:
                pass

        # Extract path & method
        curl_cmd = ""
        sample_resp_obj = None
        if pw_data and isinstance(pw_data, list):
            for sec in pw_data:
                if isinstance(sec, dict):
                    if sec.get('section_name') == "Przykład wywołania":
                        for el in sec.get('section_elements', []):
                            curl_cmd = el.get('content', '')
                    elif sec.get('section_name') == "Przykład odpowiedzi":
                        for el in sec.get('section_elements', []):
                            raw_content = el.get('content', '')
                            try:
                                sample_resp_obj = json.loads(raw_content)
                            except Exception:
                                pass

        curl_info = parse_curl_command(curl_cmd)
        path = curl_info['path']
        method = curl_info['method']
        
        if path == "/api/action/unknown":
            # Fallback path if cURL parsing was missing
            path = f"/api/action/get_{ds_name.replace('-', '_')}"

        schema_name = f"{to_camel_case(ds_name)}Response"

        # Construct response schema
        response_schema = None
        if pa_data and isinstance(pa_data, list):
            for sec in pa_data:
                if isinstance(sec, dict) and sec.get('section_name') == "Parametry odpowiedzi":
                    elements = sec.get('section_elements', [])
                    response_schema = build_schema_from_pa(elements)

        # Infer fallback schema if pa_data had no details
        if not response_schema or not response_schema.get("properties"):
            if sample_resp_obj is not None:
                response_schema = build_schema_from_json_sample(sample_resp_obj)
            else:
                response_schema = {"type": "object", "description": "Generic response payload"}

        # Store schema component
        openapi_doc["components"]["schemas"][schema_name] = response_schema

        # Build path operation
        if path not in openapi_doc["paths"]:
            openapi_doc["paths"][path] = {}

        operation = {
            "summary": ds_title,
            "description": ds_notes or ds_title,
            "tags": [ds_category],
            "operationId": f"get{to_camel_case(ds_name)}",
            "responses": {
                "200": {
                    "description": "Successful operation",
                    "content": {
                        "application/json": {
                            "schema": {
                                "$ref": f"#/components/schemas/{schema_name}"
                            }
                        }
                    }
                }
            }
        }

        # Extract request parameters from pa_data or cURL body
        req_params = []
        if pa_data and isinstance(pa_data, list):
            for sec in pa_data:
                if isinstance(sec, dict) and sec.get('section_name') == "Parametry wywołania":
                    for el in sec.get('section_elements', []):
                        p_name = el.get('name')
                        p_type = el.get('type', 'string')
                        p_desc = el.get('description', '')
                        if p_name and p_type != "token":
                            param_schema = map_type_to_openapi(p_type)
                            param_obj = {
                                "name": p_name,
                                "in": "query",
                                "description": p_desc,
                                "required": False,
                                "schema": param_schema
                            }
                            req_params.append(param_obj)

        if req_params:
            operation["parameters"] = req_params

        # Add sample request body if method is POST and body sample exists
        if method == "post" and curl_info.get("sample_body"):
            body_schema = build_schema_from_json_sample(curl_info["sample_body"])
            operation["requestBody"] = {
                "description": "Query parameters for dataset filtering",
                "required": False,
                "content": {
                    "application/json": {
                        "schema": body_schema,
                        "example": curl_info["sample_body"]
                    }
                }
            }

        openapi_doc["paths"][path][method] = operation
        processed_count += 1

    # 3. Write output files
    print(f"[3/4] Writing OpenAPI specification to {DOCS_DIR}...")
    yaml_path = DOCS_DIR / "openapi.yaml"
    json_path = DOCS_DIR / "openapi.json"

    with open(yaml_path, 'w', encoding='utf-8') as f:
        yaml.dump(openapi_doc, f, allow_unicode=True, sort_keys=False)

    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(openapi_doc, f, indent=2, ensure_ascii=False)

    print(f"      Generated: {yaml_path}")
    print(f"      Generated: {json_path}")

    # 4. Validate specification
    print("[4/4] Validating OpenAPI specification with openapi-spec-validator...")
    try:
        from openapi_spec_validator import validate
        validate(openapi_doc)
        print("SUCCESS: Generated OpenAPI specification is valid and fully compliant with 3.1.0 standards!")
    except Exception as e:
        print(f"WARNING: Specification validation warning/error: {e}")

if __name__ == '__main__':
    main()
