package org.schemeguard.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.entity.User;
import org.schemeguard.backend.repository.UserRepository;
import org.schemeguard.backend.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = UUID.fromString(
                    jwtService.extractUserId(token)
            );

            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user != null
                    && "ACTIVE".equals(user.getStatus())
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                String role = jwtService.extractRole(token);

                if (role == null || role.isBlank()) {
                    role = "MERCHANT";
                }

                var authorities = List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                );

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (IllegalArgumentException ignored) {
            // Token invalid; request remains unauthenticated.
        }

        filterChain.doFilter(request, response);
    }
}