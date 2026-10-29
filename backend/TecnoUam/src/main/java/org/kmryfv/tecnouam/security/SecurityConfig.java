package org.kmryfv.tecnouam.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:8087"));
    private final List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Usuarios de prueba
    @Bean
    UserDetailsService usuarios(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin")
                        .password(encoder.encode("Admin123!"))
                        .roles("ADMIN")
                        .build(),

                User.withUsername("usuario")
                        .password(encoder.encode("Usuario123!"))
                        .roles("USER")
                        .build()
        );
    }

    // Administrador de autenticación
    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService usuarios,
            PasswordEncoder encoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(usuarios);

        provider.setPasswordEncoder(encoder);

        return new ProviderManager(provider);
    }

    // Clave secreta JWT
    @Bean
    SecretKey jwtSecretKey() {
        String base64 = System.getenv("JWT_SECRET_BASE64");

        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException(
                    "Configure JWT_SECRET_BASE64 en IntelliJ");
        }

        byte[] clave = Base64.getDecoder().decode(base64);

        if (clave.length < 32) {
            throw new IllegalStateException(
                    "La clave HS256 debe tener al menos 32 bytes");
        }

        return new SecretKeySpec(clave, "HmacSHA256");
    }

    // Generación de tokens JWT
    @Bean
    JwtEncoder jwtEncoder(SecretKey clave) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(clave)
        );
    }

    // Validación de tokens JWT
    @Bean
    JwtDecoder jwtDecoder(SecretKey clave) {
        return NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // Conversión de roles del JWT
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    // Reglas de seguridad de la API
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos
                        .requestMatchers("/api/auth/**", "/api/publico/**").permitAll()

                        // Consultar equipos y préstamos: USER y ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/equipos/**", "/api/prestamos/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Crear, modificar, eliminar equipos, registrar préstamos y devoluciones: solo ADMIN
                        .requestMatchers("/api/equipos/**", "/api/prestamos/**")
                        .hasRole("ADMIN")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                )
                .build();
    }
}
