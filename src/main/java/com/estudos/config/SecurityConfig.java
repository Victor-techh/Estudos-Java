package com.estudos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final FirebaseTokenFilter firebaseTokenFilter;

  public SecurityConfig(FirebaseTokenFilter firebaseTokenFilter) {
    this.firebaseTokenFilter = firebaseTokenFilter;
  }

  private static final String[] SWAGGER_WHITELIST = {
      "/swagger-ui/**",
      "/swagger-ui.html",
      "/v3/api-docs",
      "/v3/api-docs/**"
  };

  private static final String[] WEB_WHITELIST = {
      "/",
      "/index.html",
      "/login.html",
      "/register.html",
      "/painel.html",
      "/list.html",
      "/firebase-config.js",
      "/*.js",
      "/*.html",
      "/css/**",
      "/js/**",
      "/images/**",
      "/favicon.ico"
  };

  private static final String[] AUTH_ROUTES = {
      "/api/auth/login",
      "/api/auth/cadastro"
  };

  private static final String[] ADMIN_ROUTES = {
      "/admin.html",
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(SWAGGER_WHITELIST).permitAll()
            .requestMatchers(WEB_WHITELIST).permitAll()
            .requestMatchers(AUTH_ROUTES).permitAll()
            .requestMatchers(ADMIN_ROUTES).hasRole("ADMIN")
            .anyRequest().authenticated())
        .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}