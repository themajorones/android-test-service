package dev.themajorones.autotest.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import dev.themajorones.autotest.security.CustomOAuth2FailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        CustomOAuth2SuccessHandler successHandler,
        CustomOAuth2FailureHandler failureHandler
    ) throws Exception {

        http.cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/auth/**",
                    "/health",
                    "/",
                    "/index.html",
                    "/assets/**",
                    "/oauth2/**",
                    "/login/**",
                    "/actuator/**"
                ).permitAll()
                .requestMatchers("/api/connections/**", "/api/task-logs").permitAll()
                .requestMatchers(HttpMethod.POST, "/webhook/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/rabbitmq/messages").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration apiCors = new CorsConfiguration();
        apiCors.setAllowedOriginPatterns(List.of("*"));
        apiCors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        apiCors.setAllowedHeaders(List.of("*"));
        apiCors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/connections/**", apiCors);
        source.registerCorsConfiguration("/api/task-logs", apiCors);
        return source;
    }
}
