package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        return mapToDto(requestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestDto> findByRequestor(Long userId) {
        if (!userRepository.existsById(userId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Пользователь не найден");
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return mapToDtoList(requests);
    }

    @Override
    public List<ItemRequestDto> findAllByOtherUsers(Long userId) {
        if (!userRepository.existsById(userId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Пользователь не найден");
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        return mapToDtoList(requests);
    }

    @Override
    public ItemRequestDto findById(Long requestId, Long userId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запрос не найден"));
        return mapToDto(request, itemRepository.findAllByRequestId(requestId));
    }

    private List<ItemRequestDto> mapToDtoList(List<ItemRequest> requests) {
        if (requests.isEmpty()) return List.of();
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemsByRequest = items.stream().
                collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream().map(req -> mapToDto(req, itemsByRequest.getOrDefault(req.getId(),
                List.of()))).toList();
    }

    private ItemRequestDto mapToDto(ItemRequest req, List<Item> items) {
        List<ItemForRequestDto> itemDtos = items.stream().map(item -> ItemForRequestDto.builder()
                .id(item.getId()).name(item.getName()).ownerId(item.getOwner().getId()).build()).toList();
        return ItemRequestDto.builder()
                .id(req.getId()).description(req.getDescription()).created(req.getCreated()).items(itemDtos).build();
    }
}