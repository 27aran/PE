# REST API and SPA Example Project

> This repository contains an example of a web application based on a RESTful API with Spring Boot (`api/`) and a single page application with Vue.js (`frontend/`).
> Please refer to the `README.md` files in the respective folders for concrete instructions.


## How to Start

### 1. Terminal öffnen
Öffnen Sie ein Terminal (Linux/Mac) oder die Eingabeaufforderung/PowerShell (Windows) im Hauptverzeichnis dieses Repositories.

### 2. In das API-Verzeichnis wechseln
```bash
cd api
```
### 3. Anwendung starten (Backend)
#### Linux / macOS
```bash
./mvnw spring-boot:run
```
#### Windows
```bash
mvnw spring-boot:run
```
Die Anwendung wird standardmäßig auf Port **8080.**
- Base URL: http://localhost:8080

## Tests ausführen
#### Linux / macOS
```bash
./mvnw clean test
```
#### Windows
```bash
mvnw clean test
```
