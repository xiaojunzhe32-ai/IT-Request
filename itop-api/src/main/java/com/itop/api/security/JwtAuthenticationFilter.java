package com.itop.api.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itop.core.entity.Role;
import com.itop.core.entity.User;
import com.itop.core.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtService.isTokenValid(token)) {

                User user = userRepository.findByUsernameWithRoles(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (!"active".equalsIgnoreCase(user.getStatus()) || Boolean.TRUE.equals(user.getLocked())) {
                    throw new RuntimeException("User account is disabled or locked");
                }

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                if (user.getRoles() != null) {
                    for (Role role : user.getRoles()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
                        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
                            try {
                                List<String> permissions = objectMapper.readValue(
                                        role.getPermissions(),
                                        new TypeReference<List<String>>() {}
                                );
                                List<GrantedAuthority> permissionAuthorities = permissions.stream()
                                        .filter(p -> !p.equals("*"))
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());
                                authorities.addAll(permissionAuthorities);

                                if (permissions.contains("*")) {
                                    authorities.add(new SimpleGrantedAuthority("ALL_PERMISSIONS"));
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}