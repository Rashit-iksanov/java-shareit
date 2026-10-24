package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void create_shouldSaveAndReturnDto() {
        User user = User.builder().id(1L).build();
        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder().description("Need drill").build();
        ItemRequest savedRequest = ItemRequest.builder().id(10L)
                .description("Need drill").requestor(user).created(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);

        ItemRequestDto result = requestService.create(1L, createDto);

        assertNotNull(result);
        assertEquals("Need drill", result.getDescription());
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void create_userNotFound_shouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                () -> requestService.create(99L, new ItemRequestCreateDto()));
    }

    @Test
    void findAllByOtherUsers_shouldReturnRequests() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(1L)).thenReturn(List.of());

        List<ItemRequestDto> result = requestService.findAllByOtherUsers(1L);
        assertTrue(result.isEmpty());
    }
}