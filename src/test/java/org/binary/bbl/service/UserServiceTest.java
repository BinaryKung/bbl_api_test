package org.binary.bbl.service;

import org.binary.bbl.dto.UserInput;
import org.binary.bbl.entity.User;
import org.binary.bbl.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Tanasat Tangudomkarn");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Example User");

        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_withValidId_shouldReturnUser() {
        // Given
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setName("Tanasat Tangudomkarn");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Tanasat Tangudomkarn", result.getName());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_withInvalidId_shouldThrowNotFoundException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUserById(userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void createUser_withValidInput_shouldCreateUser() {
        // Given
        UserInput input = new UserInput();
        input.setName("Tanasat Tangudomkarn");
        input.setUsername("binarykung");
        input.setEmail("binary_kung@outlook.com");
        input.setPhone("0830076641");
        input.setWebsite("https://binarykung.com");

        when(userRepository.existsByUsernameOrEmail(input.getUsername(), input.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.createUser(input);

        // Then
        verify(userRepository, times(1)).existsByUsernameOrEmail(input.getUsername(), input.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_withExistingUsername_shouldThrowConflictException() {
        // Given
        UserInput input = new UserInput();
        input.setName("Tanasat Tangudomkarn");
        input.setUsername("binarykung");
        input.setEmail("binary_kung@outlook.com");

        when(userRepository.existsByUsernameOrEmail(input.getUsername(), input.getEmail())).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(input));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username or email already exists", exception.getReason());
        verify(userRepository, times(1)).existsByUsernameOrEmail(input.getUsername(), input.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_withExistingEmail_shouldThrowConflictException() {
        // Given
        UserInput input = new UserInput();
        input.setName("Tanasat Tangudomkarn");
        input.setUsername("binarykung");
        input.setEmail("binary_kung@outlook.com");

        when(userRepository.existsByUsernameOrEmail(input.getUsername(), input.getEmail())).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(input));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username or email already exists", exception.getReason());
        verify(userRepository, times(1)).existsByUsernameOrEmail(input.getUsername(), input.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_withValidInput_shouldUpdateUser() {
        // Given
        Long userId = 1L;
        UserInput input = new UserInput();
        input.setName("Updated Name");
        input.setUsername("newusername");
        input.setEmail("new_binary_kung@outlook.com");
        input.setPhone("0830076641");
        input.setWebsite("https://newexample.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("binarykung");
        existingUser.setEmail("binary_kung@outlook.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameOrEmail(input.getUsername(), input.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.updateUser(userId, input);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByUsernameOrEmail(input.getUsername(), input.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_withInvalidId_shouldThrowNotFoundException() {
        // Given
        Long userId = 999L;
        UserInput input = new UserInput();
        input.setName("Updated Name");
        input.setUsername("username");
        input.setEmail("email@outlook.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(userId, input));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_withExistingUsernameOrEmail_shouldThrowConflictException() {
        // Given
        Long userId = 1L;
        UserInput input = new UserInput();
        input.setName("Updated Name");
        input.setUsername("existingusername");
        input.setEmail("existing@outlook.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("oldusername");
        existingUser.setEmail("old@outlook.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsernameOrEmail(input.getUsername(), input.getEmail())).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(userId, input));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Username or email already exists", exception.getReason());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByUsernameOrEmail(input.getUsername(), input.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_withSameUsernameAndEmail_shouldUpdateUser() {
        // Given
        Long userId = 1L;
        UserInput input = new UserInput();
        input.setName("Updated Name");
        input.setUsername("sameusername");
        input.setEmail("same@outlook.com");
        input.setPhone("9876543210");
        input.setWebsite("https://newexample.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("sameusername");
        existingUser.setEmail("same@outlook.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.updateUser(userId, input);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByUsernameOrEmail(anyString(), anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser_withValidId_shouldDeleteUser() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_withInvalidId_shouldThrowNotFoundException() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.deleteUser(userId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}
