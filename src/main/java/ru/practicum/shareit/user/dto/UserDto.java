package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.OnCreate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "Имя не может быть пустым", groups = OnCreate.class)
    private String name;

    @NotBlank(message = "Email не может быть пустым", groups = OnCreate.class)
    @Email(message = "Некорректный формат email", groups = OnCreate.class)
    private String email;
}
