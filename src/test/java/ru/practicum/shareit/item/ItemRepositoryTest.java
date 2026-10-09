package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findAllByOwnerId_shouldReturnOnlyOwnerItems() {
        User owner = entityManager.persist(User.builder().name("Owner").email("owner@test.com").build());
        User other = entityManager.persist(User.builder().name("Other").email("other@test.com").build());
        entityManager.flush();

        Item item1 = Item.builder().name("Drill").description("Desc").available(true).owner(owner).build();
        Item item2 = Item.builder().name("Hammer").description("Desc").available(true).owner(other).build();

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        List<Item> result = itemRepository.findAllByOwnerId(owner.getId());
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void search_shouldReturnAvailableItemsMatchingTextIgnoreCase() {
        User owner = entityManager.persist(User.builder().name("Owner").email("owner@test.com").build());
        entityManager.flush();

        Item availableDrill = Item.builder().name("Super Drill").description("Powerful").available(true).owner(owner).build();
        Item unavailableDrill = Item.builder().name("Old Drill").description("Broken").available(false).owner(owner).build();
        Item hammer = Item.builder().name("Hammer").description("Tool").available(true).owner(owner).build();

        entityManager.persist(availableDrill);
        entityManager.persist(unavailableDrill);
        entityManager.persist(hammer);
        entityManager.flush();

        List<Item> result = itemRepository.search("drill");

        assertEquals(1, result.size());
        assertEquals("Super Drill", result.get(0).getName());
    }
}