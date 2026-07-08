package com.example.movra.config.security;

import com.example.movra.bc.account.user.application.user.internal.TokenService;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.CustomOauthService;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.handler.OauthFailureHandler;
import com.example.movra.bc.account.user.infrastructure.user.security.oauth.handler.OauthSuccessHandler;
import com.example.movra.config.exception.GlobalExceptionFilter;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenFilter;
import com.example.movra.bc.account.user.infrastructure.user.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final CustomOauthService customOauthService;
    private final OauthSuccessHandler oauthSuccessHandler;
    private final OauthFailureHandler oauthFailureHandler;

    @Value("${app.cors.allowed-origin-patterns}")
    private List<String> allowedOriginPatterns;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception{
        return http
                .csrf(CsrfConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/oauth/complete").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/oauth/profile-setup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reissue").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/onboarding-context").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOauthService))
                        .successHandler(oauthSuccessHandler)
                        .failureHandler(oauthFailureHandler))
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider, tokenService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new GlobalExceptionFilter(objectMapper), JwtTokenFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(resolveAllowedOriginPatterns());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> resolveAllowedOriginPatterns() {
        if (allowedOriginPatterns == null) {
            throw new IllegalStateException("CORS allowed origin patterns must be configured.");
        }

        List<String> normalizedPatterns = allowedOriginPatterns.stream()
                .map(String::trim)
                .filter(pattern -> !pattern.isBlank())
                .toList();

        if (normalizedPatterns.isEmpty() || normalizedPatterns.contains("*")) {
            throw new IllegalStateException("Credentialed CORS must not use wildcard origins.");
        }

        return normalizedPatterns;
    }
}
