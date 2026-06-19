package com.docsignature.backend.service;

import com.docsignature.backend.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {
    @Autowired
    private AppProperties properties;

    @Override
    public StoredFile store(MultipartFile file, String prefix) throws IOException {
        Path dir = Paths.get(properties.storageDir());
        Files.createDirectories(dir);
        String storedFilename = prefix + "-" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        Path path = dir.resolve(storedFilename);
        Files.copy(file.getInputStream(), path);
        return new StoredFile(storedFilename, path);
    }

    @Override
    public Resource loadAsResource(String storedFilename) throws IOException {
        Path path = resolve(storedFilename);
        return new FileSystemResource(path);
    }

    @Override
    public Path resolve(String storedFilename) {
        return Paths.get(properties.storageDir()).resolve(storedFilename).normalize();
    }

    private String sanitize(String originalFilename) {
        return originalFilename == null ? "document.pdf" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
