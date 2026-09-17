package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto dto, Long userId);

    ItemDto update(ItemDto dto, Long userId, Long itemId);

    ItemDto findById(Long itemId);

    List<ItemDto> findAllByOwnerId(Long userId);

    List<ItemDto> search(String text);
}
