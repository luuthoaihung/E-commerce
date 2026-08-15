package com.ecommerce.identity.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${file.temp-dir}")
    private String tempDir;

    public String uploadFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // 1. Đảm bảo thư mục tạm tồn tại
            Files.createDirectories(Paths.get(tempDir));

            // 2. Chuyển MultipartFile sang File vật lý trong thư mục tạm
            File convertedFile = new File(tempDir + File.separator + fileName);
            try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
                fos.write(file.getBytes());
            }

            // 3. Tối ưu/Nén ảnh trước khi đẩy lên S3 (Sử dụng Thumbnailator)
            try {
                Thumbnails.of(convertedFile)
                          .size(800, 800)
                          .outputQuality(0.8)
                          .toFile(convertedFile);
            } catch (Exception e) {
                // Nếu file không phải là ảnh hợp lệ, bỏ qua nén và dùng file gốc
            }

            // 4. Đẩy lên S3
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, convertedFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            // 5. Xóa file tạm sau khi upload thành công
            Files.delete(convertedFile.toPath());

            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi upload ảnh lên S3: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;
        try {
            // Trích xuất tên file từ URL (lấy phần sau dấu / cuối cùng)
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            amazonS3.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            System.err.println("Không thể xóa ảnh cũ trên S3: " + e.getMessage());
        }
    }
}