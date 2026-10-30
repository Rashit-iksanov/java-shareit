package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeAndDeserializeUserDto() throws Exception {
        UserDto dto = UserDto.builder().id(1L).name("Ivan").email("ivan@test.com").build();

        String json = objectMapper.writeValueAsString(dto);
        UserDto deserialized = objectMapper.readValue(json, UserDto.class);

        assertNotNull(deserialized);
        assertEquals("Ivan", deserialized.getName());
        assertEquals("ivan@test.com", deserialized.getEmail());
    }
}