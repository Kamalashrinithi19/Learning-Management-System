package com.lms.service;

import com.lms.dto.CreateUserRequest;
import com.lms.dto.UserResponse;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.exception.DuplicateResourceException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private User sampleUser;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .fullName("Test User")
                .email("test@lms.com")
                .password("encodedPassword")
                .role(Role.STUDENT)
                .active(true)
                .build();

        createRequest = new CreateUserRequest();
        createRequest.setFullName("Test User");
        createRequest.setEmail("test@lms.com");
        createRequest.setPassword("password123");
        createRequest.setRole(Role.STUDENT);
    }

    @Test
    @DisplayName("Create user successfully")
    void createUser_Success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@lms.com");
        assertThat(response.getFullName()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create user fails on duplicate email")
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("test@lms.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Get all users returns list")
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("test@lms.com");
    }

    @Test
    @DisplayName("Get user by ID returns user")
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Get user by non-existent ID throws exception")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Delete user removes from repository")
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(sampleUser);
    }

    @Test
    @DisplayName("Password is hashed and not stored as plain text")
    void createUser_PasswordIsHashed() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            assertThat(u.getPassword()).isEqualTo("$2a$hash");
            assertThat(u.getPassword()).doesNotContain("password123");
            return sampleUser;
        });

        userService.createUser(createRequest);

        verify(passwordEncoder).encode("password123");
    }
}
