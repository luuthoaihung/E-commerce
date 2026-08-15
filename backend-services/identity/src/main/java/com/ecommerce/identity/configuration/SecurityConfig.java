package com.ecommerce.identity.configuration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.ecommerce.identity.dto.request.IntrospectRequest;
import com.ecommerce.identity.service.AuthService;

import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    // Danh sách các API công khai không cần Token
    private final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/introspect",
            "/api/auth/logout",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            
            
    };
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        );
    }

    @Value("${jwt.signerKey}")
    private String SIGNING_KEY;

    
   @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, JwtDecoder jwtDecoder) throws Exception {
        httpSecurity
            .cors(Customizer.withDefaults()) // <-- BẮT BUỘC PHẢI THÊM DÒNG NÀY ĐỂ CHO PHÉP CORS QUA SECURITY
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(request ->
                request.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
            );

        httpSecurity.oauth2ResourceServer(oauth2 ->
            oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        return httpSecurity.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // Đổi tiền tố mặc định thành ROLE_

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    // Hàm giải mã Token sử dụng thuật toán mã hóa HS256 và Signer Key của tụi mình
    @Bean
    public JwtDecoder jwtDecoder(@Lazy AuthService authService) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNING_KEY.getBytes(), "HS256");
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        return token -> {
            try {
                // 1. Phải giải mã và xác thực chữ ký/hạn sử dụng của token TRƯỚC
                Jwt decodedJwt = jwtDecoder.decode(token);

                // 2. (Tuỳ chọn) Sau khi decode thành công, nếu bạn muốn check xem token có bị thu hồi hay không thì gọi introspect ở đây
                var response = authService.introspect(
                        IntrospectRequest.builder().token(token).build()
                );

                if (!response.isValid()) {
                    throw new JwtException("Token không hợp lệ hoặc đã đăng xuất!");
                }

                return decodedJwt; // Trả về JWT đã giải mã thành công cho Spring Security
            } catch (Exception e) {
                throw new JwtException(e.getMessage());
            }
        };
    }

    
    
}