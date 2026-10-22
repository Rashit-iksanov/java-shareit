package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class ItemRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Test
    void findAllByRequestorIdOrderByCreatedDesc_shouldReturnSortedRequests() {
        User user = entityManager.persist(User.builder().name("Test").email("test@test.com").build());
        entityManager.flush();

        ItemRequest oldReq = ItemRequest.builder().description("Old").requestor(user)
                .created(LocalDateTime.now().minusDays(2)).build();
        ItemRequest newReq = ItemRequest.builder().description("New").requestor(user)
                .created(LocalDateTime.now()).build();

        entityManager.persist(oldReq);
        entityManager.persist(newReq);
        entityManager.flush();

        List<ItemRequest> result = requestRepository.findAllByRequestorIdOrderByCreatedDesc(user.getId());

        assertEquals(2, result.size());
        assertEquals("New", result.get(0).getDescription());
        assertEquals("Old", result.get(1).getDescription());
    }

    @Test
    void findAllByRequestorIdNotOrderByCreatedDesc_shouldExcludeCurrentUser() {
        User user1 = entityManager.persist(User.builder().name("User1").email("u1@test.com").build());
        User user2 = entityManager.persist(User.builder().name("User2").email("u2@test.com").build());
        entityManager.flush();

        ItemRequest req1 = ItemRequest.builder().description("Req1").requestor(user1)
                .created(LocalDateTime.now()).build();
        ItemRequest req2 = ItemRequest.builder().description("Req2").requestor(user2)
                .created(LocalDateTime.now().minusHours(1)).build();

        entityManager.persist(req1);
        entityManager.persist(req2);
        entityManager.flush();

        List<ItemRequest> result = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(user1.getId());

        assertEquals(1, result.size());
        assertEquals("Req2", result.get(0).getDescription());
    }
}