package ru.practicum.shareit.booking;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static BookingState from(String stringState) {
        for (BookingState state : BookingState.values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return state;
            }
        }
        throw new IllegalStateException("Unknown state: " + stringState);
    }
}
