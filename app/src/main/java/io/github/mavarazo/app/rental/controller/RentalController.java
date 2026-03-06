package io.github.mavarazo.app.rental.controller;

import io.github.mavarazo.app.rental.model.RentalConfirmationDto;
import io.github.mavarazo.app.rental.model.RentalRequestDto;
import io.github.mavarazo.app.rental.service.RentalCancelService;
import io.github.mavarazo.app.rental.service.RentalCreateService;
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
    private final RentalCancelService rentalCancelService;

    @PostMapping
    public ResponseEntity<RentalConfirmationDto> create(@RequestBody final RentalRequestDto rentalRequestDto) {
        return ResponseEntity.ok(rentalCreateService.createRental(rentalRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable final UUID id) {
        rentalCancelService.cancelRental(id);
        return ResponseEntity.noContent().build();
    }
}
