package dev.themajorones.autotest.config;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

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

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    pathPattern("/auth/**"),
                    pathPattern("/health"),
                    pathPattern("/"),
                    pathPattern("/index.html"),
                    pathPattern("/assets/**"),
                    pathPattern("/oauth2/**"),
                    pathPattern("/login/**"),
                    pathPattern("/actuator/**")
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            );

        return http.build();
    }
}
