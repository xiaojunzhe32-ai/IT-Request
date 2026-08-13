package com.itop.api.controller;

import com.itop.api.dto.UserDTO;
import com.itop.api.security.SecurityUtils;
import com.itop.common.dto.ApiResponse;
import com.itop.core.entity.User;
import com.itop.core.repository.OrganizationRepository;
import com.itop.core.repository.RoleRepository;
import com.itop.core.repository.TeamRepository;
import com.itop.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SecurityUtils securityUtils;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(
                userRepository,
                organizationRepository,
                roleRepository,
                teamRepository,
                passwordEncoder,
                securityUtils
        );
        lenient().when(securityUtils.getAccessibleOrgIds()).thenReturn(null);
    }

    @Test
    void statusUpdateSetsRequestedStateInsteadOfToggling() {
        User user = user(2L, "operator", "inactive", User.AuthMethod.LOCAL);
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        UserController.StatusUpdateRequest request = new UserController.StatusUpdateRequest();
        request.setStatus("inactive");
        ResponseEntity<ApiResponse<UserDTO>> response = controller.setStatus(2L, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(user.getStatus()).isEqualTo("inactive");
        verify(userRepository).save(user);
    }

    @Test
    void userCannotDisableOwnAccount() {
        User user = user(1L, "admin", "active", User.AuthMethod.LOCAL);
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        UserController.StatusUpdateRequest request = new UserController.StatusUpdateRequest();
        request.setStatus("inactive");
        ResponseEntity<ApiResponse<UserDTO>> response = controller.setStatus(1L, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("cannot disable your own");
        verify(userRepository, never()).save(any());
    }

    @Test
    void nonAdminCannotAssignAdminRole() {
        when(userRepository.existsByUsername("new-user")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(organizationRepository.existsById(100L)).thenReturn(true);
        when(securityUtils.isAdmin()).thenReturn(false);

        UserDTO dto = UserDTO.builder()
                .username("new-user")
                .email("new@example.com")
                .organizationId(100L)
                .authMethod("LOCAL")
                .password("secret123")
                .admin(true)
                .build();
        ResponseEntity<ApiResponse<UserDTO>> response = controller.create(dto);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("Only admins can grant admin access");
        verify(userRepository, never()).save(any());
    }

    @Test
    void passwordResetRejectsExternalAuthenticationAccounts() {
        User user = user(3L, "ldap-user", "active", User.AuthMethod.LDAP);
        when(userRepository.findById(3L)).thenReturn(java.util.Optional.of(user));

        UserController.PasswordResetRequest request = new UserController.PasswordResetRequest();
        request.setNewPassword("secret123");
        ResponseEntity<ApiResponse<Void>> response = controller.resetPassword(3L, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("only available for local accounts");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    private User user(Long id, String username, String status, User.AuthMethod authMethod) {
        User user = new User(username, username + "@example.com");
        user.setId(id);
        user.setStatus(status);
        user.setAuthMethod(authMethod);
        user.setRoles(List.of());
        return user;
    }
}
