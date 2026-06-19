package com.docsignature.backend.controller;

import com.docsignature.backend.domain.UserEntity;
import com.docsignature.backend.dto.SignatureDtos;
import com.docsignature.backend.exception.ApiException;
import com.docsignature.backend.repository.UserRepository;
import com.docsignature.backend.service.SignatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class SignatureController {
    @Autowired
    private SignatureService signatureService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/api/signatures")
    public SignatureDtos.SignatureRequestResponse create(
            Authentication authentication,
            @Valid @RequestBody SignatureDtos.CreateSignatureRequest request,
            HttpServletRequest servletRequest
    ) {
        UserEntity owner = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return signatureService.create(owner, request, servletRequest.getRemoteAddr());
    }

    @GetMapping("/api/signatures/{docId}")
    public SignatureDtos.SignatureRequestResponse get(Authentication authentication, @PathVariable Long docId) {
        UserEntity owner = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return signatureService.getByDocument(owner, docId);
    }

    @PostMapping("/api/signatures/finalize")
    public SignatureDtos.SignatureRequestResponse finalize(
            Authentication authentication,
            @Valid @RequestBody SignatureDtos.FinalizeSignatureRequest request,
            HttpServletRequest servletRequest
    ) throws IOException {
        UserEntity actor = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return signatureService.finalizeByToken(request.token(), request.action(), request.signatureText(), request.rejectionReason(), servletRequest.getRemoteAddr(), actor);
    }

    @GetMapping("/api/public/signatures/{token}")
    public SignatureDtos.PublicSigningResponse getPublic(@PathVariable String token) {
        return signatureService.getPublic(token);
    }

    @GetMapping("/api/public/signatures/{token}/download")
    public ResponseEntity<Resource> downloadPublic(@PathVariable String token) throws IOException {
        SignatureDtos.PublicSigningResponse response = signatureService.getPublic(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + response.originalFilename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(signatureService.downloadPublic(token));
    }

    @PostMapping("/api/public/signatures/{token}/finalize")
    public SignatureDtos.SignatureRequestResponse finalizePublic(
            @PathVariable String token,
            @RequestBody SignatureDtos.FinalizeSignatureRequest request,
            HttpServletRequest servletRequest
    ) throws IOException {
        return signatureService.finalizeByToken(token, request.action(), request.signatureText(), request.rejectionReason(), servletRequest.getRemoteAddr(), null);
    }

    @GetMapping("/api/signatures/me")
    public List<SignatureDtos.SignatureRequestResponse> list(Authentication authentication) {
        UserEntity owner = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return signatureService.listForOwner(owner);
    }
}
