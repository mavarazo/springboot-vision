package io.github.mavarazo.vision.rental.controller;

import io.github.mavarazo.vision.rental.model.RentalConfirmationDto;
import io.github.mavarazo.vision.rental.model.RentalRequestDto;
import io.github.mavarazo.vision.rental.service.RentalCreateService;
import io.github.mavarazo.vision.rental.service.RetnalCancelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalCreateService rentalCreateService;
    private final RetnalCancelService retnalCancelService;

    @PostMapping
    public ResponseEntity<RentalConfirmationDto> create(@RequestBody final RentalRequestDto rentalRequestDto) {
        return ResponseEntity.ok(rentalCreateService.createRental(rentalRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable final UUID id) {
        retnalCancelService.cancelRental(id);
        return ResponseEntity.noContent().build();
    }
}
