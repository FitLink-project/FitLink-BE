package com.fitlink.web.controller;

import com.fitlink.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.ok(fileStorageService.uploadFile(multipartFile));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.ok("Deleted: " + fileName);
    }

    @DeleteMapping("/delete-by-url")
    public ResponseEntity<String> deleteFileByUrl(@RequestParam String fileUrl) {
        fileStorageService.deleteFileByUrl(fileUrl);
        return ResponseEntity.ok("Deleted: " + fileUrl);
    }

    @GetMapping("/url")
    public ResponseEntity<String> getFileUrl(@RequestParam String fileName) {
        return ResponseEntity.ok(fileStorageService.getFileUrl(fileName));
    }
}

