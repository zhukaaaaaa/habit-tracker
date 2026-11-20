package com.example.habittracker.security;

import java.io.IOException;
import java.util.Objects;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import com.example.habittracker.service.JwtService;
import com.example.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;
    private final ApiAuthenticationEntryPoint entryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var claims = jwtService.parseToken(header.substring(7));
                var user = userService.loadUserByUsername(claims.getSubject());
                if (!Objects.equals(user.id(), claims.get("userId", Long.class))) {
                    throw new BadCredentialsException("Неверный токен");
                }
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                entryPoint.commence(request, response, new BadCredentialsException("Неверный токен"));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
