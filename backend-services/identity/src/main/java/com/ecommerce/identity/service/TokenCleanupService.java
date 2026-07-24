package com.ecommerce.identity.service;

import com.ecommerce.identity.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TokenCleanupService {

    InvalidatedTokenRepository invalidatedTokenRepository;
    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional 
    public void deleteExpiredTokens() {
        invalidatedTokenRepository.deleteAllByExpiryTimeBefore(new Date());
        log.info("--- [LAO CÔNG HỆ THỐNG]: Đã dọn dẹp sạch sẽ các Token hết hạn ngầm dưới DB! ---");
    }
}