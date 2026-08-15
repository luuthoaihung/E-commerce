package com.ecommerce.identity.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ecommerce.identity.dto.response.ApiResponse;
import com.ecommerce.identity.service.S3Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FileController {

    private final S3Service s3Service;

    // Thêm consumes = MediaType.MULTIPART_FORM_DATA_VALUE vào đây
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = s3Service.uploadFile(file);
        return ApiResponse.<String>builder()
                .result(fileUrl)
                .message("Upload ảnh lên S3 thành công")
                .build();
    }
}