package io.github.mavarazo.vision.rental.model;

import java.time.LocalDate;
import java.util.UUID;

public record BookingConfirmationDto(UUID id, UUID vehicleId, UUID insuranceId, LocalDate from, LocalDate upto) {
}
