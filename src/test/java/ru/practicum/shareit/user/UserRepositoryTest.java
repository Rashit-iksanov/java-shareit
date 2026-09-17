package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {
    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void save_shouldAssignIdAndSaveUser() {
        User user = new User(null, "Ivan", "ivan@test.com");
        User saved = repository.save(user);

        assertNotNull(saved.getId());
        assertEquals("ivan@test.com", saved.getEmail());
        assertEquals(1L, repository.findAll().size());
    }

    @Test
    void existsByEmail_shouldReturnTrueIfExists() {
        repository.save(new User(1L, "Ivan", "ivan@test.com"));
        assertTrue(repository.existsByEmail("ivan@test.com"));
        assertTrue(repository.existsByEmail("IVAN@TEST.COM"));
        assertFalse(repository.existsByEmail("other@test.com"));
    }
}