package com.docsignature.backend.controller;

import com.docsignature.backend.domain.UserEntity;
import com.docsignature.backend.dto.DocumentDtos;
import com.docsignature.backend.exception.ApiException;
import com.docsignature.backend.repository.UserRepository;
import com.docsignature.backend.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDtos.UploadResponse upload(
            Authentication authentication,
            @RequestPart("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request
    ) throws IOException {
        UserEntity owner = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return new DocumentDtos.UploadResponse(documentService.upload(owner, file, title, description, request.getRemoteAddr()));
    }

    @GetMapping
    public List<DocumentDtos.DocumentSummary> list(Authentication authentication) {
        UserEntity owner = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return documentService.list(owner);
    }

    @GetMapping("/{id}")
    public DocumentDtos.DocumentDetail get(Authentication authentication, @PathVariable Long id, HttpServletRequest request) {
        UserEntity owner = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        return documentService.get(owner, id, request.getRemoteAddr());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(Authentication authentication, @PathVariable Long id) throws IOException {
        UserEntity owner = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));
        Resource resource = documentService.download(owner, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
