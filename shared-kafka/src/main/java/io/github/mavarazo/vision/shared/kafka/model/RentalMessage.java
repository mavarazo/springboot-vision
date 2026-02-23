package io.github.mavarazo.vision.shared.kafka.model;

public record RentalMessage(java.util.UUID id, java.util.UUID vehicleId, java.util.UUID insuranceId,
                            java.time.LocalDate from, java.time.LocalDate upto) {
}
