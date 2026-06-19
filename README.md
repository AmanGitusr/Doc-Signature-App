# Document Signature App – Project Presentation Script

## Introduction

Hello,

 My **Document Signature App** is a secure, enterprise-grade digital signature platform inspired by solutions such as DocuSign and Adobe Sign.

The application allows users to upload PDF documents, place digital signatures, share signing links with recipients, track document status, and generate signed PDFs with a complete audit trail.

The primary goal of this project was to gain hands-on experience with enterprise Java development while solving real-world document workflow challenges.

---

## Problem Statement

In many organizations, document signing is still handled manually through printed documents, emails, or scanned signatures.

This creates several issues:

* Slow approval processes
* Lack of transparency
* Risk of document tampering
* Difficulty tracking document status
* Missing compliance and audit records

My application addresses these problems through secure digital signatures, workflow tracking, and immutable signed PDFs.

---

## Technology Stack

### Backend

The backend is built using:

* Java 17
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA with Hibernate
* MySQL Database
* Maven

### PDF Processing

For document handling, I used:

* Apache PDFBox
* Multipart File Upload Support

### Frontend

The frontend is developed using:

* React
* Tailwind CSS
* React PDF
* Drag-and-Drop functionality using dnd-kit

---

## Application Workflow

### Step 1: User Authentication

Users can register and log in securely.

Passwords are encrypted using BCrypt, and authentication is handled through JWT tokens.

Spring Security protects all private APIs and ensures that only authorized users can access documents.

---

### Step 2: Document Upload

After logging in, users can upload PDF documents.

The application stores:

* File metadata
* Owner information
* Upload timestamp
* Document status

The actual file is securely managed by the backend.

---

### Step 3: Document Preview

Users can view uploaded documents directly within the application.

The React PDF viewer renders document pages and provides a smooth preview experience.

---

### Step 4: Signature Placement

Users can drag and drop signature placeholders onto specific locations within the PDF.

The system records:

* Page number
* X coordinate
* Y coordinate
* Signer information
* Signature status

These coordinates are stored in the database for later PDF generation.

---

### Step 5: Signing Process

Recipients receive a secure tokenized signing link.

Using this link, they can review and sign the document without needing direct access to the application.

The token ensures secure and controlled access.

---

### Step 6: Signed PDF Generation

Once signatures are completed, the application uses Apache PDFBox to embed signature information into the PDF.

A new signed PDF is generated and stored.

The document becomes immutable, ensuring its integrity.

---

### Step 7: Audit Trail

Every important action is logged, including:

* Upload events
* Document views
* Signature actions
* Rejections
* Status updates

Each log contains timestamps and user information to support compliance and traceability.

---

## Key Features

The major features of this application include:

* Secure JWT Authentication
* PDF Upload and Management
* Drag-and-Drop Signature Placement
* Public Signing Links
* Signed PDF Generation
* Audit Logging
* Document Status Tracking
* Responsive User Interface

---

## Database Design

The application contains several core entities:

### User

Stores:

* ID
* Name
* Email
* Password

### Document

Stores:

* Document ID
* File Information
* Owner
* Upload Date
* Current Status

### Signature

Stores:

* Document Reference
* Signer Information
* Coordinates
* Page Number
* Signature Status

### Audit Log

Stores:

* Action Type
* User
* Timestamp
* Additional Metadata

---

## Security Considerations

Security was one of the primary focuses of this project.

Implemented measures include:

* JWT Authentication
* BCrypt Password Encryption
* Protected REST APIs
* Token-Based Public Signing Links
* Role-Based Access Control
* Audit Logging

These features ensure the platform can securely handle sensitive business documents.

---

## Challenges Faced

Some key challenges included:

* Capturing accurate signature coordinates on PDF pages
* Synchronizing frontend placement with backend PDF generation
* Implementing secure public signing links
* Managing document workflow states
* Ensuring audit trail consistency

These challenges provided valuable experience in enterprise application development.
---

📧 Contact
Developer: Aman Tiwari

