package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_shouldSaveUserWhenEmailIsUnique() {
        UserDto dto = new UserDto(null, "Ivan", "ivan@test.com");
        User user = new User(1L, "Ivan", "ivan@test.com");

        when(userRepository.existsByEmail("ivan@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(dto);

        assertNotNull(result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_withExistingEmail_shouldThrowException() {
        UserDto dto = new UserDto(null, "Ivan", "ivan@test.com");
        when(userRepository.existsByEmail("ivan@test.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(dto));
        assertEquals("Пользователь с таким email уже существует", exception.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldChangeFieldsCorrectly() {
        User existing = new User(1L, "Ivan", "ivan@test.com");
        UserDto dto = new UserDto(1L, "Ivan Petrov", "new@test.com");
        User updated = new User(1L, "Ivan Petrov", "new@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updated);

        UserDto result = userService.update(dto, 1L);

        assertEquals("Ivan Petrov", result.getName());
        assertEquals("new@test.com", result.getEmail());
    }

    @Test
    void findById_notFound_shouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.findById(99L));
        assertEquals("Пользователь не найден", exception.getReason());
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        Long userId = 1L;
        userService.delete(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }
}