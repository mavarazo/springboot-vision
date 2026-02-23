package io.github.mavarazo.vision.shared.kafka.model;

import java.util.UUID;

public record AccidentMessage(UUID id, UUID vehicleId) {
}
