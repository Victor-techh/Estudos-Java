package com.estudos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .authorizeHttpRequests(auth -> auth
            // Público
            .requestMatchers(
                "/login.html",
                "/register.html",
                "/css/**",
                "/js/**",
                "/images/**")
            .permitAll()

            // Administração
            .requestMatchers("/admin.html")
            .hasRole("ADMIN")

            // Qualquer outra rota
            .anyRequest()
            .authenticated())

        .formLogin(form -> form
            .loginPage("/login.html")
            .defaultSuccessUrl("/", true)
            .permitAll());
    return http.build();
  }
}