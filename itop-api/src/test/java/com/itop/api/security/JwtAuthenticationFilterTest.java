package com.itop.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itop.core.entity.User;
import com.itop.core.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private FilterChain filterChain;

    @Test
    void disabledUserWithValidTokenIsNotAuthenticated() throws Exception {
        User user = new User("disabled", "disabled@example.com");
        user.setStatus("inactive");
        user.setLocked(false);
        user.setRoles(List.of());

        when(jwtService.extractUsername("valid-token")).thenReturn("disabled");
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(userRepository.findByUsernameWithRoles("disabled")).thenReturn(Optional.of(user));

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
