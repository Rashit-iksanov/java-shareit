package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = UserMapper.toUser(dto);
        User savedUser = userRepository.save(user);
        log.info("Создан новый пользователь с id={}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(UserDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Пользователь с таким email уже существует");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        User updatedUser = userRepository.save(user);
        log.info("Обновлен пользователь с id={}", userId);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
