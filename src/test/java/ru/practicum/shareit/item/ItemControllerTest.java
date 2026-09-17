package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturn201AndUseHeader() throws Exception {
        ItemDto dto = new ItemDto(null, "Drill", "Desc", true, null);
        ItemDto response = new ItemDto(1L, "Drill", "Desc", true, null);
        when(itemService.create(any(ItemDto.class), eq(1L))).thenReturn(response);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_withoutHeader_shouldReturn400() throws Exception {
        ItemDto dto = new ItemDto(null, "Drill", "Desc", true, null);
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllByOwnerId_shouldReturn200() throws Exception {
        when(itemService.findAllByOwnerId(1L)).thenReturn(List.of(
                new ItemDto(1L, "Drill", "Desc", true, null)
        ));
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void search_shouldHandleQueryParam() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(
                new ItemDto(1L, "Дрель", "Мощная", true, null)
        ));
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void delete_byOwner_shouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_withoutHeader_shouldReturn400BadRequest() throws Exception {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isBadRequest());
    }
}