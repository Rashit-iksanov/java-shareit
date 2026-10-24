package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(BookingDto dto, Long bookerId) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        if (!item.getAvailable()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь недоступна");
        if (item.getOwner().getId().equals(bookerId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Владелец не может бронировать свою вещь");
        if (!dto.getStart().isBefore(dto.getEnd()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные даты");

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long bookingId, boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
        if (!booking.getItem().getOwner().getId().equals(ownerId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только владелец может подтверждать");
        if (booking.getStatus() != BookingStatus.WAITING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус уже изменен");
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findByBooker(Long bookerId, BookingState state, Integer from, Integer size) {
        if (!userRepository.existsById(bookerId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Пользователь не найден");
        PageRequest pageRequest = PageRequest.of(from / size, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "start"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerId(bookerId, pageRequest);
            case CURRENT -> bookingRepository.findCurrentByBookerId(bookerId, now, pageRequest);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBefore(bookerId, now, pageRequest);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfter(bookerId, now, pageRequest);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING, pageRequest);
            case REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, pageRequest);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> findByOwner(Long ownerId, BookingState state, Integer from, Integer size) {
        if (!userRepository.existsById(ownerId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        PageRequest pageRequest = PageRequest.of(from / size, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "start"));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByOwnerId(ownerId, pageRequest);
            case CURRENT -> bookingRepository.findCurrentByOwnerId(ownerId, now, pageRequest);
            case PAST -> bookingRepository.findAllByOwnerIdAndEndBefore(ownerId, now, pageRequest);
            case FUTURE -> bookingRepository.findAllByOwnerIdAndStartAfter(ownerId, now, pageRequest);
            case WAITING -> bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageRequest);
            case REJECTED -> bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageRequest);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }
}