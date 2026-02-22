package io.github.mavarazo.vision.rental.model;

import java.time.LocalDate;
import java.util.UUID;

public record BookingRequestDto(UUID vehicleId, LocalDate from, LocalDate upto) {
}
