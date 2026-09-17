package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRepositoryTest {
    private InMemoryItemRepository repository;
    private User owner;

    @BeforeEach
    void setUp() {
        repository = new InMemoryItemRepository();
        owner = new User(1L, "Owner", "owner@test.com");
    }

    @Test
    void findAllByOwnerId_shouldReturnOnlyOwnerItems() {
        Item item1 = new Item(1L, "Drill", "Desc", true, owner, null);
        Item item2 = new Item(2L, "Hammer", "Desc", true, new User(2L,
                "Other", "other@test.com"), null);

        repository.save(item1);
        repository.save(item2);

        List<Item> result = repository.findAllByOwnerId(1L);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }
}