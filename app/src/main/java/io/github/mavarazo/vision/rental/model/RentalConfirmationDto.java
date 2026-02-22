package io.github.mavarazo.vision.rental.model;

import java.time.LocalDate;
import java.util.UUID;

public record RentalConfirmationDto(UUID id, UUID vehicleId, UUID insuranceId, LocalDate from, LocalDate upto) {
}
