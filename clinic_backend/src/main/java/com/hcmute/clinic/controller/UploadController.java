package com.hcmute.clinic.controller;

import com.hcmute.clinic.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

/**
 * Controller quản lý việc tải lên tập tin và hình ảnh phục vụ các nghiệp vụ trong hệ thống.
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    /**
     * Xử lý yêu cầu tải lên một tập tin từ người dùng.
     *
     * @param file Tập tin cần tải lên.
     * @return ResponseEntity chứa thông tin về tập tin đã tải lên bao gồm tên, đường dẫn tải xuống, loại và kích thước.
     */
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "fileDownloadUri", fileDownloadUri,
                "fileType", file.getContentType() != null ? file.getContentType() : "unknown",
                "size", file.getSize()
        ));
    }
}
