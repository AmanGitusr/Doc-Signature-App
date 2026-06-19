# Document Signature Web App

Local-first full-stack document signature platform built with:

- Backend: Java 17, Spring Boot, Spring Security, Spring Data JPA, Flyway, PDFBox
- Frontend: React, Vite, React Router, react-pdf
- Database: MySQL for normal runs, H2 for tests

## Repository Layout

- `backend/` Spring Boot API
- `frontend/` React client

## Quick Start

1. Create a MySQL database named `doc_signature_app`.
2. Set backend environment variables:
   - `DB_URL=jdbc:mysql://localhost:3306/doc_signature_app`
   - `DB_USERNAME=...`
   - `DB_PASSWORD=...`
   - `JWT_SECRET=change-this-secret`
   - `UPLOAD_DIR=./storage/uploads`
3. Run the backend:
   - `cd backend`
   - `mvn spring-boot:run`
4. Run the frontend:
   - `cd frontend`
   - `npm install`
   - `npm run dev`

## Architecture Overview

- Users register and authenticate with JWT.
- Owners upload PDF documents and create a signing request for a single recipient.
- The signer follows a public tokenized link to approve or reject the document.
- The backend stamps the PDF and updates document and signature status.
- All important actions are recorded in audit events.

## API Summary

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/docs/upload`
- `GET /api/docs`
- `GET /api/docs/{id}`
- `GET /api/docs/{id}/download`
- `POST /api/signatures`
- `GET /api/signatures/{docId}`
- `POST /api/signatures/finalize`
- `GET /api/public/signatures/{token}`
- `POST /api/public/signatures/{token}/finalize`
- `GET /api/audit/{docId}`

## Deployment Notes

- No Docker is used in this project.
- For production, point the backend at a managed MySQL instance and set `APP_BASE_URL`, `CORS_ALLOWED_ORIGINS`, and `JWT_SECRET`.
- Use a writable `UPLOAD_DIR` for file storage.
