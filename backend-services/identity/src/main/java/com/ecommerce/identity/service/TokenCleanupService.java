package com.ecommerce.identity.service;

import com.ecommerce.identity.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TokenCleanupService {

    InvalidatedTokenRepository invalidatedTokenRepository;

    // 🌟 Cấu hình thời gian chạy ngầm: Ở đây dùng Cron Expression
    // "0 0 0 * * ?" nghĩa là đúng 00:00:00 đêm mỗi ngày tác vụ này sẽ tự kích hoạt
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional // Bắt buộc phải có vì đây là thao tác xóa dữ liệu (Delete)
    public void deleteExpiredTokens() {
        // Gọi repo xóa toàn bộ token có hạn dùng nhỏ hơn thời điểm hiện tại
        invalidatedTokenRepository.deleteAllByExpiryTimeBefore(new Date());
        System.out.println("--- [LAO CÔNG HỆ THỐNG]: Đã dọn dẹp sạch sẽ các Token hết hạn ngầm dưới DB! ---");
    }
}