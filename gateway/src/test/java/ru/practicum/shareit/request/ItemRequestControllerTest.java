package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestClient requestClient;

    @Test
    void create_shouldCallClientAndReturn200() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder().description("Need drill").build();
        when(requestClient.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.ok().body("{\"id\":1}"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_withBlankDescription_shouldReturn400() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder().description("   ").build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllByOtherUsers_shouldCallClient() throws Exception {
        when(requestClient.findAllByOtherUsers(1L)).thenReturn(ResponseEntity.ok().body("[]"));

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}