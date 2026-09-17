package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto dto) {
        log.info("Создание вещи пользователем с id={}", userId);
        return itemService.create(dto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @Valid @RequestBody ItemDto dto) {
        log.info("Обновление вещи с id={} пользователем с id={}", itemId, userId);
        return itemService.update(dto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId) {
        log.info("Получение вещи с id={}", itemId);
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemDto> findAllByOwnerId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получение списка вещей пользователя с id={}", userId);
        return itemService.findAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Поиск вещей по тексту: {}", text);
        return itemService.search(text);
    }
}
