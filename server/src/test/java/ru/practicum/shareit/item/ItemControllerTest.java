package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        ItemDto dto = ItemDto.builder().name("Drill").description("Desc").available(true).build();
        ItemDto response = ItemDto.builder().id(1L).name("Drill").description("Desc").available(true).build();

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
        ItemDto dto = ItemDto.builder().name("Drill").description("Desc").available(true).build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllByOwnerId_shouldReturn200() throws Exception {
        ItemDto response = ItemDto.builder().id(1L).name("Drill").description("Desc").available(true).build();
        when(itemService.findAllByOwnerId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void search_shouldHandleQueryParam() throws Exception {
        ItemDto response = ItemDto.builder().id(1L).name("Дрель").description("Мощная").available(true).build();
        when(itemService.search("дрель")).thenReturn(List.of(response));

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

    @Test
    void addComment_shouldReturn200() throws Exception {
        CommentDto response = CommentDto.builder().id(1L).text("Good").authorName("Ivan").build();
        when(itemService.addComment(eq(1L), any(CommentDto.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CommentDto.builder().text("Good").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("Ivan"));
    }
}