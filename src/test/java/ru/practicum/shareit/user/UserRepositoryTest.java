package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_shouldAssignIdAndSaveUser() {
        User user = User.builder().name("Ivan").email("ivan@test.com").build();
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("ivan@test.com", saved.getEmail());
        assertEquals(1L, userRepository.findAll().size());
    }

    @Test
    void existsByEmailIgnoreCase_shouldReturnTrueIfExists() {
        entityManager.persist(User.builder().name("Ivan").email("ivan@test.com").build());
        entityManager.flush();

        assertTrue(userRepository.existsByEmailIgnoreCase("ivan@test.com"));
        assertTrue(userRepository.existsByEmailIgnoreCase("IVAN@TEST.COM"));
        assertFalse(userRepository.existsByEmailIgnoreCase("other@test.com"));
    }
}