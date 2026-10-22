package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void create_shouldSaveBookingWithWaitingStatus() {
        User booker = User.builder().id(1L).build();
        User owner = User.builder().id(2L).build();
        Item item = Item.builder().id(10L).available(true).owner(owner).build();
        BookingDto dto = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingDto result = bookingService.create(dto, 1L);

        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void create_byOwner_shouldThrowConflictException() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder().id(10L).available(true).owner(owner).build();
        BookingDto dto = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.create(dto, 1L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateStatus_byOwner_shouldApprove() {
        User owner = User.builder().id(1L).build();
        User booker = User.builder().id(2L).build();
        Item item = Item.builder().id(10L).owner(owner).build();

        Booking booking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingDto result = bookingService.updateStatus(100L, true, 1L);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void updateStatus_byNonOwner_shouldThrowForbidden() {
        User owner = User.builder().id(1L).build();
        User other = User.builder().id(2L).build();
        User booker = User.builder().id(3L).build();
        Item item = Item.builder().id(10L).owner(owner).build();

        Booking booking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingService.updateStatus(100L, true, 2L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}