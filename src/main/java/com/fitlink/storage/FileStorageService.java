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

    @Value("${file.base-url:}")
    private String fileBaseUrl;

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
        String fileName;
        
        // 절대 URL인 경우 (http:// 또는 https://로 시작)
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            // base-url이 설정되어 있으면 base-url을 제거
            if (StringUtils.hasText(fileBaseUrl)) {
                String baseUrl = fileBaseUrl.endsWith("/") 
                        ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) 
                        : fileBaseUrl;
                if (fileUrl.startsWith(baseUrl)) {
                    // base-url 제거 후 상대 경로 추출
                    String relativePath = fileUrl.substring(baseUrl.length());
                    if (relativePath.startsWith(normalizedPrefix)) {
                        fileName = relativePath.substring(normalizedPrefix.length());
                    } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 URL입니다.");
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 URL입니다.");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 URL입니다.");
            }
        } else if (fileUrl.startsWith(normalizedPrefix)) {
            // 상대 경로인 경우
            fileName = fileUrl.substring(normalizedPrefix.length());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 파일 URL입니다.");
        }

        deleteFile(fileName);
    }

    public String getFileUrl(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일명이 필요합니다.");
        }
        return buildFileUrl(fileName);
    }

    /**
     * 기존 URL(상대 경로 또는 절대 경로)을 절대 URL로 변환합니다.
     * base-url이 설정되어 있으면 절대 URL로 변환하고, 없으면 원본을 반환합니다.
     *
     * @param url 변환할 URL (상대 경로 또는 절대 경로)
     * @return 절대 URL 또는 원본 URL
     */
    public String convertToAbsoluteUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }

        // 이미 절대 URL인 경우 (http:// 또는 https://로 시작)
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        // base-url이 설정되어 있지 않으면 원본 반환
        if (!StringUtils.hasText(fileBaseUrl)) {
            return url;
        }

        // 상대 경로인 경우 절대 URL로 변환
        String normalizedUrl = url.startsWith("/") ? url : "/" + url;
        String baseUrl = fileBaseUrl.endsWith("/") 
                ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) 
                : fileBaseUrl;
        return baseUrl + normalizedUrl;
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
        String relativePath = ensureTrailingSlash(fileUrlPrefix) + fileName;
        
        // base-url이 설정되어 있으면 절대 URL 반환, 없으면 상대 경로 반환
        if (StringUtils.hasText(fileBaseUrl)) {
            // baseUrl 끝의 슬래시 제거 후 상대 경로 추가 (상대 경로가 이미 /로 시작하므로)
            String baseUrl = fileBaseUrl.endsWith("/") 
                    ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) 
                    : fileBaseUrl;
            return baseUrl + relativePath;
        }
        
        return relativePath;
    }

    private String ensureTrailingSlash(String path) {
        if (path.endsWith("/")) {
            return path;
        }
        return path + "/";
    }
}

