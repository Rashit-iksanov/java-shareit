package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDto dto, Long bookerId);

    BookingDto updateStatus(Long bookingId, boolean approved, Long ownerId);

    BookingDto findById(Long bookingId, Long userId);

    List<BookingDto> findByBooker(Long bookerId, BookingState state);

    List<BookingDto> findByOwner(Long ownerId, BookingState state);
}
