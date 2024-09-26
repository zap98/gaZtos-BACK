package com.example.gAZtos.Config;

import com.example.gAZtos.Entities.PasswordResetToken;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Services.AuthService;
import com.example.gAZtos.Utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Lazy
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Lista de paths que no requieren autenticación
        List<String> excludedPaths = Arrays.asList("/api/auth/login", "/api/auth/register", "/api/auth/forgot-password");
        Optional<PasswordResetToken> user = Optional.empty();
        String userName = "";
        User userAux;

        // Si la ruta actual está en la lista de exclusiones, continuamos sin autenticar
        if (excludedPaths.contains(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.error("Empty token in request header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header must be provided");
            return;
        }

        String jwt = authorizationHeader.substring(7);
        String endPoint = request.getServletPath();

        try {
            if (endPoint.contains("/api/auth/recoveryPassword")) {
                user = jwtUtil.extractUser(jwt);
            } else {
                userName = jwtUtil.extractUsername(jwt);
            }
        } catch (Exception e) {
            logger.error("Invalid token in request");
            response.getWriter().write("Invalid JWT token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (user.isPresent()) {
            userAux = authService.findByUserName(user.get().getUser().getUsername());
            validateTokenRecovery(userAux, response, jwt, user, request);
        } else if (!userName.isEmpty()) {
            userAux = authService.findByUserName(userName);
            validateToken(userAux, response, jwt, user, request);
        }
        filterChain.doFilter(request, response);
    }

    private void validateToken(User userAux, HttpServletResponse response, String jwt, Optional<PasswordResetToken> user, HttpServletRequest request) throws IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if (userAux == null) {
                logger.error("User not found from token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User not found");
                return;
            }

            if (jwtUtil.validateToken(jwt, userAux)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, userAux.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.error("Invalid JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
            }
        }
    }

    private void validateTokenRecovery(User userAux, HttpServletResponse response, String jwt, Optional<PasswordResetToken> user, HttpServletRequest request) throws IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if (userAux == null) {
                logger.error("User not found from tokenRecovery");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User not found");
                return;
            }

            if (jwtUtil.validateTokenRecovery(jwt, user)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, userAux.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.error("Invalid JWT tokenRecovery");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT tokenRecovery");
            }
        }
    }
}
