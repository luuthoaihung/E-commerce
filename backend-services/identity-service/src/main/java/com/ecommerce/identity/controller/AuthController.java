package com.ecommerce.identity.controller;

import com.ecommerce.identity.dto.request.AuthenticationRequest;
import com.ecommerce.identity.dto.request.IntrospectRequest;
import com.ecommerce.identity.dto.response.AuthenticationResponse;
import com.ecommerce.identity.dto.response.IntrospectResponse;
import com.ecommerce.identity.entity.User;
import com.ecommerce.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody User user) { // Đọc dữ liệu JSON từ Body Request truyền vào đối tượng User
        return userService.register(user);
    }

    @PostMapping("/login") // Đường dẫn đầy đủ sẽ là /api/auth/login
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        return userService.authenticate(request);
    }

    @PostMapping("/introspect")
    public IntrospectResponse introspect(@RequestBody IntrospectRequest request) {
        return userService.introspect(request);
    }
    
}