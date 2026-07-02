package com.vitaldrop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF for API development simplicity
                .csrf(csrf -> csrf.disable())

                // 2. Enforce Stateless Session Policy (Merged from WebSecurityConfig)
                // This stops Spring Security from saving session states on the server, ideal for REST APIs
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Define Unified Route Access Parameters
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to the landing page analytical counters
                        .requestMatchers("/requests/public/stats").permitAll()

                        // Allow static assets, UI templates, and user login gateways
                        .requestMatchers("/", "/index.html", "/landing-style.css", "/static/**", "/donor/login.html", "/recipient/login.html").permitAll()

                        // Allow public authentication, registration, and OTP workflows
                        .requestMatchers("/donor/register", "/donor/login", "/recipient/register", "/recipient/login").permitAll()
                        .requestMatchers("/otp/**").permitAll()

                        // Open for testing. Change to .authenticated() when wiring up JWT tokens/sessions later!
                        .anyRequest().permitAll()
                )

                // 4. Disable default browser authentication popups/login forms
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}