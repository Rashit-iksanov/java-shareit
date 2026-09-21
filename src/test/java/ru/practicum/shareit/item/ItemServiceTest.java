package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void create_shouldSetOwnerAndSave() {
        User owner = new User(1L, "Ivan", "ivan@test.com");
        ItemDto dto = new ItemDto(null, "Drill", "Powerful", true, null);
        Item savedItem = new Item(1L, "Drill", "Powerful", true, owner, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.create(dto, 1L);

        assertNotNull(result.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void update_byNonOwner_shouldThrowException() {
        User owner = new User(1L, "Ivan", "ivan@test.com");
        Item item = new Item(1L, "Drill", "Desc", true, owner, null);
        ItemDto dto = new ItemDto(1L, "New Name", "New Desc", false, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            itemService.update(dto, 2L, 1L);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Пользователь не является владельцем вещи", exception.getReason());
    }

    @Test
    void search_shouldReturnOnlyAvailableItemsAndIgnoreCase() {
        User owner = new User(1L, "Ivan", "ivan@test.com");
        Item availableDrill = new Item(1L, "Super Drill", "Desc",
                true, owner, null);
        Item unavailableDrill = new Item(2L, "Old Drill", "Desc",
                false, owner, null);
        Item hammer = new Item(3L, "Hammer", "Desc", true, owner, null);

        when(itemRepository.findAll()).thenReturn(List.of(availableDrill, unavailableDrill, hammer));

        List<ItemDto> result = itemService.search("drill");

        assertEquals(1, result.size());
        assertEquals("Super Drill", result.get(0).getName());
    }

    @Test
    void search_withBlankText_shouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("   ");
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).findAll();
    }

    @Test
    void delete_byOwner_shouldDeleteSuccessfully() {
        User owner = new User(1L, "Ivan", "ivan@test.com");
        Item item = new Item(1L, "Drill", "Desc", true, owner, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L, 1L); // Владелец (id=1) удаляет свою вещь (id=1)

        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_byNonOwner_shouldThrow403Forbidden() {
        User owner = new User(1L, "Ivan", "ivan@test.com");
        Item item = new Item(1L, "Drill", "Desc", true, owner, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            itemService.delete(2L, 1L);
        });

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Пользователь не является владельцем вещи", exception.getReason());
        verify(itemRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_nonExistingItem_shouldThrow404NotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            itemService.delete(1L, 99L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Вещь не найдена", exception.getReason());
        verify(itemRepository, never()).deleteById(anyLong());
    }
}