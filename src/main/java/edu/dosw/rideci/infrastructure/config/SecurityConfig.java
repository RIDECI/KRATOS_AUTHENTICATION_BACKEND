package edu.dosw.rideci.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security
 * - Deshabilita sesiones (usamos JWT)
 * - Permite acceso público a endpoints de auth
 * - Configura BCrypt para hashear contraseñas
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura el filtro de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF porque usamos JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Endpoints públicos
                        .requestMatchers("/actuator/**").permitAll() // Health checks
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                        .anyRequest().authenticated() // El resto requiere autenticación
                );

        return http.build();
    }
}
