package com.restaurant.orderservice.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {

            try {
                Long userId = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                Long restaurantId = jwtUtil.extractRestaurantID(token);

                JwtUserDetails userDetails = JwtUserDetails.builder()
                        .userId(userId)
                        .username(username)
                        .role(role)
                        .restaurantId(restaurantId)
                        .build();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

                log.debug(
                        "JWT auth successful for user: {} with role: {} and restaurantId: {}",
                        username,
                        role,
                        restaurantId
                );

            } catch (Exception ex) {

                log.error(
                        "Could not set authentication in security context: {}",
                        ex.getMessage()
                );

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {

        String bearer = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearer)
                && bearer.startsWith(BEARER_PREFIX)) {

            return bearer.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}