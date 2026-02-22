package io.github.mavarazo.vision.vehicle.controller;

import io.github.mavarazo.vision.vehicle.model.VehicleDto;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getVehicles() {
        final Faker faker = new Faker();
        final List<VehicleDto> vehicles = faker
                .collection(
                        () -> new VehicleDto(UUID.randomUUID(), faker.vehicle().makeAndModel())
                )
                .len(25)
                .generate();
        return ResponseEntity.ok(vehicles);
    }
}
