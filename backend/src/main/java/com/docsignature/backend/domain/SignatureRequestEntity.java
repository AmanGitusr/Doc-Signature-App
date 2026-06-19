package com.docsignature.backend.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "signature_requests")
public class SignatureRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private DocumentEntity document;

    @Column(name = "signer_name", nullable = false, length = 120)
    private String signerName;

    @Column(name = "signer_email", nullable = false, length = 180)
    private String signerEmail;

    @Column(nullable = false, unique = true, length = 80)
    private String token;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Column(name = "x_position", nullable = false)
    private double xPosition;

    @Column(name = "y_position", nullable = false)
    private double yPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SignatureStatus status;

    @Column(name = "signature_text")
    private String signatureText;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }

    public String getSignerName() {
        return signerName;
    }

    public void setSignerName(String signerName) {
        this.signerName = signerName;
    }

    public String getSignerEmail() {
        return signerEmail;
    }

    public void setSignerEmail(String signerEmail) {
        this.signerEmail = signerEmail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public double getXPosition() {
        return xPosition;
    }

    public void setXPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public double getYPosition() {
        return yPosition;
    }

    public void setYPosition(double yPosition) {
        this.yPosition = yPosition;
    }

    public SignatureStatus getStatus() {
        return status;
    }

    public void setStatus(SignatureStatus status) {
        this.status = status;
    }

    public String getSignatureText() {
        return signatureText;
    }

    public void setSignatureText(String signatureText) {
        this.signatureText = signatureText;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
}
