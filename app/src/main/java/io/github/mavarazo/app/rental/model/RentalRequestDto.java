package io.github.mavarazo.app.rental.model;

import java.time.LocalDate;
import java.util.UUID;

public record RentalRequestDto(UUID vehicleId, LocalDate from, LocalDate upto) {
}
