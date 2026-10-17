package com.estudos.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (!token.isEmpty()) {
                try {
                    String[] partesJwt = token.split("\\.");
                    if (partesJwt.length >= 2) {
                        byte[] decodedBytes = Base64.getUrlDecoder().decode(partesJwt[1]);
                        String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);
                        JsonNode claims = objectMapper.readTree(payloadJson);

                        String uid = claims.has("user_id") ? claims.get("user_id").asText()
                                : (claims.has("sub") ? claims.get("sub").asText() : "anonymous");
                        String email = claims.has("email") ? claims.get("email").asText() : uid;
                        String name = claims.has("name") ? claims.get("name").asText() : null;

                        long exp = claims.has("exp") ? claims.get("exp").asLong() : 0;
                        long currentTime = System.currentTimeMillis() / 1000;

                        if (exp == 0 || exp >= currentTime) {
                            List<SimpleGrantedAuthority> authorities = Collections
                                    .singletonList(new SimpleGrantedAuthority("ROLE_USER"));

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    email, null, authorities);

                            java.util.Map<String, Object> details = new java.util.HashMap<>();
                            details.put("uid", uid);
                            details.put("email", email);
                            if (name != null) {
                                details.put("name", name);
                            }
                            authentication.setDetails(details);

                            request.setAttribute("firebaseUid", uid);
                            request.setAttribute("firebaseEmail", email);
                            if (name != null) {
                                request.setAttribute("firebaseName", name);
                            }

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Falha ao processar token de autenticação: " + e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
