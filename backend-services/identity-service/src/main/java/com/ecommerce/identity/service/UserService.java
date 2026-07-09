package com.ecommerce.identity.service;

import com.ecommerce.identity.dto.request.AuthenticationRequest;
import com.ecommerce.identity.dto.response.AuthenticationResponse;
import com.ecommerce.identity.entity.User;
import com.ecommerce.identity.exception.AppException;
import com.ecommerce.identity.exception.ErrorCode;
import com.ecommerce.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.ecommerce.identity.dto.request.IntrospectRequest;
import com.ecommerce.identity.dto.response.IntrospectResponse;
import java.text.ParseException;
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder; // Lấy bộ mã hóa đã cấu hình bên SecurityConfig

    public String register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            // Thay vì return string, ném thẳng lỗi ra luôn!
            throw new AppException(ErrorCode.USER_EXISTED); 
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            // Ném lỗi trùng email
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Đăng ký thành công tài khoản: " + user.getUsername();
    }
    

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElse(null); 

        if (user == null) {
            return AuthenticationResponse.builder()
                    .authenticated(false)
                    .build();
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        // Nếu đúng pass thì sinh token thật, nếu sai thì trả về null
        String token = authenticated ? generateToken(user) : null;

        return AuthenticationResponse.builder()
                .authenticated(authenticated)
                .token(token)
                .build();
    }

    // 4. Viết thêm hàm tạo Token hoàn chỉnh này ở dưới cùng class:
    private String generateToken(User user) {
        // Thuật toán mã hóa đóng dấu (HS256)
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        // Nội dung bên trong Token (Claims)
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // Đại diện cho User
                .issuer("ecommerce.com")     // Nguồn phát hành token
                .issueTime(new Date())       // Thời gian tạo
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli() // Token có hiệu lực trong 1 tiếng
                ))
                .claim("userId", user.getId()) // Lưu thêm ID của user nếu cần
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            // Tiến hành ký số với chìa khóa bí mật
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize(); // Trả về chuỗi Token dạng mã hóa hoàn chỉnh
        } catch (JOSEException e) {
            throw new RuntimeException("Lỗi trong quá trình tạo Token", e);
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();

        try {
            // 1. Tạo bộ xác thực dựa trên chuỗi khóa bí mật SIGNER_KEY của server
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

            // 2. Giải mã chuỗi Token nhận được từ client
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 3. Kiểm tra xem Token đã hết hạn chưa (So với thời điểm hiện tại)
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            
            // 4. Kiểm tra tính toàn vẹn của chữ ký số
            boolean verified = signedJWT.verify(verifier);

            // Token chỉ hợp lệ khi: Chữ ký chuẩn xác VÀ chưa hết hạn sử dụng
            boolean isValid = verified && expiryTime.after(new Date());

            return IntrospectResponse.builder()
                    .valid(isValid)
                    .build();

        } catch (JOSEException | ParseException e) {
            // Nếu token sai định dạng, rách nát hoặc lỗi mã hóa, coi như không hợp lệ luôn
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }
}