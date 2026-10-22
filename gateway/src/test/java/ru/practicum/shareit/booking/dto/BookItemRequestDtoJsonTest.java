package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JsonTest
class BookItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeAndDeserializeCorrectly() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(10L,
                LocalDateTime.of(2023, 10, 25, 10, 0),
                LocalDateTime.of(2023, 10, 26, 10, 0));

        String json = objectMapper.writeValueAsString(dto);
        BookItemRequestDto deserialized = objectMapper.readValue(json, BookItemRequestDto.class);

        assertNotNull(deserialized);
        assertEquals(10L, deserialized.getItemId());
        // Проверка, что даты корректно прочитались (формат ISO-8601 по умолчанию в Spring Boot)
        assertEquals(dto.getStart().toString(), deserialized.getStart().toString());
    }
}