package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto dto) {
        log.info("Создание пользователя: {}", dto.getName());
        return userService.create(dto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @Valid @RequestBody UserDto dto) {
        log.info("Обновление пользователя с id={}", userId);
        return userService.update(dto, userId);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        log.info("Получение пользователя с id={}", userId);
        return userService.findById(userId);
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Получение списка всех пользователей");
        return userService.findAll();
    }
}
