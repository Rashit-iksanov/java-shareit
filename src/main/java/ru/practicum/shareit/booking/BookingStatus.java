package ru.practicum.shareit.booking;

public enum BookingStatus {
    WAITING,    // Новое бронирование, ожидает одобрения
    APPROVED,   // Подтверждено владельцем
    REJECTED,   // Отклонено владельцем
    CANCELED    // Отменено создателем
}
