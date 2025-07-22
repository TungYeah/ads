package vn.minhtung.ads.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import vn.minhtung.ads.util.SecutiryUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Value("${minhtung.jwt.base64-secret}")
    private String jwtKey;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecutiryUtil.JWT_ALGORITHM.getName());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withSecretKey(getSecretKey())
                .macAlgorithm(SecutiryUtil.JWT_ALGORITHM)
                .build();
    }

    @Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> permissions = jwt.getClaimAsStringList("permission");
        if (permissions != null) {
            for (String p : permissions) {
                authorities.add(new SimpleGrantedAuthority(p));
            }
        }

        Map<String, Object> user = jwt.getClaimAsMap("user");
        if (user != null) {
            Object roleObj = user.get("role");
            if (roleObj instanceof Map<?, ?> roleMap && roleMap.get("name") != null) {
                String roleName = roleMap.get("name").toString().toUpperCase();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            }
        }

        return authorities;
    });
    return converter;
}


    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {

        String[] publicEndpoints = {
                "/", "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register", "/storage/**"
        };

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(publicEndpoints).permitAll()

                // Cho phép USER và ADMIN xem quảng cáo
                .requestMatchers(HttpMethod.GET, "/api/v1/ads/**").hasAnyRole("USER", "ADMIN")

                // Chỉ ADMIN mới được thao tác ads ngoài GET
                .requestMatchers("/api/v1/ads/**").hasRole("ADMIN")

                // Chỉ ADMIN được thao tác role/permission
                .requestMatchers("/api/v1/roles/**", "/api/v1/permissions/**").hasRole("ADMIN")

                // Tất cả route khác yêu cầu xác thực
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable());

        return http.build();
    }
}
