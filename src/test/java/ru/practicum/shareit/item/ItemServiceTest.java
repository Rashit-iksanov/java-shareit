package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void create_shouldSetOwnerAndSave() {
        User owner = User.builder().id(1L).name("Ivan").email("ivan@test.com").build();
        ItemDto dto = ItemDto.builder().name("Drill").description("Powerful").available(true).build();
        Item savedItem = Item.builder().id(1L).name("Drill").description("Powerful").available(true).owner(owner).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.create(dto, 1L);

        assertNotNull(result.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void update_byNonOwner_shouldThrowException() {
        User owner = User.builder().id(1L).name("Ivan").email("ivan@test.com").build();
        Item item = Item.builder().id(1L).name("Drill").description("Desc").available(true).owner(owner).build();
        ItemDto dto = ItemDto.builder().name("New Name").description("New Desc").available(false).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            itemService.update(dto, 2L, 1L);
        });
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void search_shouldReturnOnlyAvailableItemsAndIgnoreCase() {
        User owner = User.builder().id(1L).build();
        Item availableDrill = Item.builder().id(1L).name("Super Drill")
                .description("Desc").available(true).owner(owner).build();
        Item unavailableDrill = Item.builder().id(2L).name("Old Drill")
                .description("Desc").available(false).owner(owner).build();

        when(itemRepository.search("drill")).thenReturn(List.of(availableDrill));

        List<ItemDto> result = itemService.search("drill");

        assertEquals(1, result.size());
        assertEquals("Super Drill", result.get(0).getName());
    }

    @Test
    void search_withBlankText_shouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("   ");
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(any());
    }

    @Test
    void delete_byOwner_shouldDeleteSuccessfully() {
        User owner = new User(1L, "Ivan", "ivan@test.com");
        Item item = new Item(1L, "Drill", "Desc", true, owner, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L, 1L);

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

    @Test
    void addComment_shouldSaveCommentWhenUserRentedItem() {
        User author = User.builder().id(1L).name("Ivan").build();
        Item item = Item.builder().id(1L).name("Drill").owner(User.builder().id(2L).build()).build();
        CommentDto dto = CommentDto.builder().text("Great!").build();

        Booking pastBooking = Booking.builder().status(BookingStatus.APPROVED).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(eq(1L), eq(1L),
                eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(pastBooking));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(10L);
            c.setAuthor(author);
            return c;
        });

        CommentDto result = itemService.addComment(1L, dto, 1L);

        assertEquals("Great!", result.getText());
        assertEquals("Ivan", result.getAuthorName());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void addComment_withoutPriorRental_shouldThrowException() {
        User author = User.builder().id(1L).name("Ivan").build();
        Item item = Item.builder().id(1L).name("Drill").build();
        CommentDto dto = CommentDto.builder().text("Great!").build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            itemService.addComment(1L, dto, 1L);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}