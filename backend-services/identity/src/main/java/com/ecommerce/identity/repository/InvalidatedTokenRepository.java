package com.ecommerce.identity.repository;

import com.ecommerce.identity.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    // 🌟 BỔ SUNG DÒNG NÀY: Xóa tất cả các token có expiryTime nhỏ hơn thời gian truyền vào
    void deleteAllByExpiryTimeBefore(Date date);
}