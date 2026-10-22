package ru.practicum.shareit.request.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRequestCreateDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_shouldFailWhenDescriptionIsBlank() {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder().description("   ").build();
        var violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для пустого описания");
    }

    @Test
    void validate_shouldPassWhenDescriptionIsValid() {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder().description("Need a hammer").build();
        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Валидный запрос не должен иметь ошибок валидации");
    }
}