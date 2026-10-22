package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto dto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Item item = ItemMapper.toItem(dto);
        item.setId(null);
        item.setOwner(owner);

        if (dto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запрос не найден"));
            item.setRequest(request);
        }

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto dto, Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь не является владельцем вещи");
        }
        if (dto.getName() != null && !dto.getName().isBlank()) item.setName(dto.getName());
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) item.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) item.setAvailable(dto.getAvailable());
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        ItemDto dto = ItemMapper.toItemDto(item);
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);
        dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findAllByOwnerId(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Booking> bookings = bookingRepository.findAllByItemIdInAndStatus(itemIds,
                BookingStatus.APPROVED, (Pageable) Sort.by(Sort.Direction.ASC, "start"));
        List<Comment> comments = commentRepository.findAllByItemIdIn(itemIds);

        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        Map<Long, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream().map(item -> {
            ItemDto dto = ItemMapper.toItemDto(item);
            enrichItemWithBookings(dto, bookingsByItem.getOrDefault(item.getId(), List.of()));
            dto.setComments(commentsByItem.getOrDefault(item.getId(), List.of()).stream()
                    .map(CommentMapper::toCommentDto).toList());
            return dto;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.search(text).stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    @Transactional
    public void delete(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь не является владельцем вещи");
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long authorId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> validBookings = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(itemId,
                authorId, BookingStatus.APPROVED, now);
        if (validBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        Comment comment = Comment.builder().text(commentDto.getText()).item(item).author(author)
                        .created(now).build();
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void enrichItemWithBookings(ItemDto dto, List<Booking> bookings) {
        if (bookings.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        Booking last = null, next = null;
        for (Booking b : bookings) {
            if (!b.getEnd().isAfter(now)) last = b;
            else if (!b.getStart().isBefore(now) && next == null) next = b;
        }
        if (last != null) dto.setLastBooking(BookingMapper.toBookingShortDto(last));
        if (next != null) dto.setNextBooking(BookingMapper.toBookingShortDto(next));
    }
}