package com.ecommerce.identity.configuration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
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
    private String SIGNER_KEY;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, JwtDecoder jwtDecoder) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers( PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());

        // 🌟 Cập nhật lại dòng này để ăn bộ converter phía dưới
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                    );
                        

        httpSecurity.csrf(csrf -> csrf.disable());
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
//  Truyền @Lazy UserService trực tiếp vào làm tham số của hàm
    public JwtDecoder jwtDecoder(@Lazy AuthService authService) {
        return token -> {
            // 🌟 SỬA: Nếu không có token, không ném lỗi ngay mà trả về null hoặc xử lý tĩnh
            // Điều này giúp Spring Security không bị chặn cứng khi gọi các API công khai (như Swagger)
            if (token == null || token.isBlank()) {
                throw new JwtException("Token is missing"); 
            }

            try {
                var response = authService.introspect(
                        IntrospectRequest.builder().token(token).build()
                );
                
                if (!response.isValid()) {
                    throw new JwtException("Token không hợp lệ hoặc đã đăng xuất!");
                }
            } catch (Exception e) {
                // Thay vì ném lỗi làm sập toàn bộ request, ta log lại
                throw new JwtException(e.getMessage());
            }

            SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS256");
            return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build()
                    .decode(token);
        };
    }

    
    
}