package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(ItemDto dto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        log.info("Создана новая вещь с id={}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(ItemDto dto, Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));

        // Проверяю, что редактировать может только владелец
        if (!item.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Пользователь не является владельцем вещи");
        }

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) item.setAvailable(dto.getAvailable());

        Item updatedItem = itemRepository.save(item);
        log.info("Обновлена вещь с id={}", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Вещь не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findAllByOwnerId(Long userId) {
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lowerText = text.toLowerCase();

        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerText) ||
                        item.getDescription().toLowerCase().contains(lowerText))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
