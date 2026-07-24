package com.ecommerce.identity.service;

import com.ecommerce.identity.dto.request.ChangePasswordRequest;
import com.ecommerce.identity.dto.request.ProfileRequest;
import com.ecommerce.identity.dto.request.UserCreationRequest;
import com.ecommerce.identity.dto.response.ProfileResponse;
import com.ecommerce.identity.dto.response.UserResponse;
import com.ecommerce.identity.entity.Role;
import com.ecommerce.identity.entity.User;
import com.ecommerce.identity.exception.AppException;
import com.ecommerce.identity.exception.ErrorCode;
import com.ecommerce.identity.mapper.UserMapper;
import com.ecommerce.identity.repository.RoleRepository;
import com.ecommerce.identity.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    EmailService emailService;
    
    @SuppressWarnings("null")
    public String register(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED); // Dùng AppException chuẩn hệ thống thay vì RuntimeException chung chung

        // 🌟 Kiểm tra email đã tồn tại hay chưa để tránh lỗi trùng lặp khi quên mật khẩu
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        // Đảm bảo bắt lỗi nếu chưa khởi tạo Role 'USER' trong database
        Role userRole = roleRepository.findById("USER")
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
        return "User registered successfully!";
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }
    @SuppressWarnings("null")
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        
        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet()));
        }
        return response;
    }

    

    public ProfileResponse getMyProfile() {
        // Lấy context từ JWT đã được xác thực qua Filter
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toProfileResponse(user);
    }
    public ProfileResponse updateProfile(ProfileRequest request) {
        // 1. Lấy user đang đăng nhập từ SecurityContext
        System.out.println("DEBUG EMAIL: " + (request != null ? request.getEmail() : "REQUEST IS NULL"));
        System.out.println("DEBUG FULLNAME: " + (request != null ? request.getFullName() : "REQUEST IS NULL"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Gán trực tiếp dữ liệu từ request sang entity (Kiểm soát tuyệt đối không sợ null)
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        // 3. Lưu xuống database
        userRepository.save(user);

        // 4. Trả về Response DTO bằng mapper thông thường (toProfileResponse)
        return userMapper.toProfileResponse(user);
    }

    public void changePassword(ChangePasswordRequest request) {
    // 1. Lấy username của user đang đăng nhập từ SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Kiểm tra mật khẩu cũ có khớp không
        boolean isPasswordMatch = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (!isPasswordMatch) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. Mã hóa và cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Sinh mã OTP ngẫu nhiên 6 chữ số
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        
        user.setOtp(otp);
        user.setOtpExpiryDate(LocalDateTime.now().plusMinutes(5)); // Hết hạn sau 5 phút
        userRepository.save(user);

        // Gửi email
        emailService.sendOtpEmail(email, otp);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new AppException(ErrorCode.INVALID_OTP); // Cần định nghĩa mã lỗi này trong ErrorCode
        }

        if (user.getOtpExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED); // Cần định nghĩa mã lỗi này trong ErrorCode
        }

        // Cập nhật mật khẩu mới và xóa OTP cũ
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiryDate(null);
        userRepository.save(user);
    }
}