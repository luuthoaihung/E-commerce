package com.ecommerce.identity.service;

import com.ecommerce.identity.dto.request.*;
import com.ecommerce.identity.dto.response.*;
import com.ecommerce.identity.entity.*;
import com.ecommerce.identity.exception.*;
import com.ecommerce.identity.repository.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


    //login ( check role)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .refreshToken(generateRefreshToken(user))
                .authenticated(true)
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        // 1. Kiểm tra tính hợp lệ (blacklist + chữ ký + expiration)
        var introspectResponse = introspect(IntrospectRequest.builder().token(request.getToken()).build());
        if (!introspectResponse.isValid()) throw new AppException(ErrorCode.UNAUTHENTICATED);

        // 2. Xác thực lại và lấy thông tin từ token cũ
        SignedJWT signedJWT = verifyToken(request.getToken());
        
        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // 3. Vô hiệu hóa token cũ (blacklist)
       // Khởi tạo đối tượng trước
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        // Sau đó mới save
        if (invalidatedToken != null) {
            invalidatedTokenRepository.save(invalidatedToken);
        }
        // 4. Sinh cặp token mới
        var user = userRepository.findByUsername(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .refreshToken(generateRefreshToken(user))
                .authenticated(true)
                .build();
    }

    public String generateToken(User user) {
        return signToken(createClaimsSet(user, 1, ChronoUnit.HOURS));
    }

    private String generateRefreshToken(User user) {
        return signToken(createClaimsSet(user, 7, ChronoUnit.DAYS));
    }

    private JWTClaimsSet createClaimsSet(User user, long time, ChronoUnit unit) {
        return new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("ecommerce.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(time, unit).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
    }

    private String signToken(JWTClaimsSet jwtClaimsSet) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimsSet.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Lỗi ký token", e);
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(request.getToken());
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            
            boolean isVerified = signedJWT.verify(verifier);
            boolean isExpired = signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date());
            boolean isInvalidated = invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID());
            
            return IntrospectResponse.builder().valid(isVerified && !isExpired && !isInvalidated).build();
        } catch (Exception e) {
            return IntrospectResponse.builder().valid(false).build();
        }
    }

    public void logout(LogoutRequest request) {
        // 1. Kiểm tra null cho request đầu vào
        if (request == null || request.getToken() == null) {
            return; // Hoặc ném ra một Exception "Invalid request"
        }

        try {
            // 2. Kiểm tra signedJWT trả về
            SignedJWT signedJWT = verifyToken(request.getToken());
            if (signedJWT == null || signedJWT.getJWTClaimsSet() == null) {
                return;
            }

            // 3. Lấy thông tin an toàn
            var claims = signedJWT.getJWTClaimsSet();
            
                    InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(claims.getJWTID())
                .expiryTime(claims.getExpirationTime())
                .build();


        // Sau đó mới save
        if (invalidatedToken != null) {
            invalidatedTokenRepository.save(invalidatedToken);
        }
        } catch (Exception e) {
            // 4. Log lỗi thay vì để trống, giúp bạn debug sau này
            log.error("Logout failed: {}", e.getMessage()); 
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (user.getRoles() != null) user.getRoles().forEach(r -> stringJoiner.add(r.getName()));
        return stringJoiner.toString();
    }
    
    // ĐÃ FIX: Hàm này giờ đây thực sự kiểm tra chữ ký
    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        
        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Chữ ký sai hoặc token bị sửa
        }
        return signedJWT; 
    }
}