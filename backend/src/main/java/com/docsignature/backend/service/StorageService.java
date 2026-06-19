package com.docsignature.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface StorageService {
    StoredFile store(MultipartFile file, String prefix) throws IOException;
    Resource loadAsResource(String storedFilename) throws IOException;
    Path resolve(String storedFilename);

    record StoredFile(String storedFilename, Path path) {}
}
