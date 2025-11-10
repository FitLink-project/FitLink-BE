package com.fitlink.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.dir}")
    private String fileDir;

    @Value("${file.url}")
    private String fileUrlPrefix;

    public String uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 존재하지 않습니다.");
        }

        String fileName = createFileName(multipartFile.getOriginalFilename());
        Path targetPath = getUploadRoot().resolve(fileName).normalize();

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.", e);
        }

        return buildFileUrl(fileName);
    }

    public void deleteFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제할 파일명이 필요합니다.");
        }

        Path targetPath = getUploadRoot().resolve(fileName).normalize();
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다.", e);
        }
    }

    public void deleteFileByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제할 파일 URL이 필요합니다.");
        }

        String normalizedPrefix = ensureTrailingSlash(fileUrlPrefix);
        if (!fileUrl.startsWith(normalizedPrefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 URL입니다.");
        }

        String fileName = fileUrl.substring(normalizedPrefix.length());
        deleteFile(fileName);
    }

    public String getFileUrl(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일명이 필요합니다.");
        }
        return buildFileUrl(fileName);
    }

    private Path getUploadRoot() {
        try {
            Path root = Paths.get(fileDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
            return root;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 경로를 생성할 수 없습니다.", e);
        }
    }

    private String createFileName(String originalName) {
        String extension = getFileExtension(originalName);
        return UUID.randomUUID() + extension;
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private String buildFileUrl(String fileName) {
        return ensureTrailingSlash(fileUrlPrefix) + fileName;
    }

    private String ensureTrailingSlash(String path) {
        if (path.endsWith("/")) {
            return path;
        }
        return path + "/";
    }
}

