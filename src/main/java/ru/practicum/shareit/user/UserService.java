package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto dto);

    UserDto update(UserDto dto, Long userId);

    UserDto findById(Long userId);

    Collection<UserDto> findAll();

    void delete(Long userId);
}
