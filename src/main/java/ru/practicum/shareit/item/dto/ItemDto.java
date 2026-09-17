package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.OnCreate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Название не может быть пустым", groups = OnCreate.class)
    private String name;

    @NotBlank(message = "Описание не может быть пустым", groups = OnCreate.class)
    private String description;

    @NotNull(message = "Статус доступности должен быть указан", groups = OnCreate.class)
    private Boolean available;

    private Long requestId;
}
