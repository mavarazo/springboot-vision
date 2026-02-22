package io.github.mavarazo.vision.rental.controller;

import io.github.mavarazo.vision.rental.model.BookingConfirmationDto;
import io.github.mavarazo.vision.rental.model.BookingRequestDto;
import io.github.mavarazo.vision.rental.service.BookingCancelService;
import io.github.mavarazo.vision.rental.service.BookingCreateService;
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

    private final BookingCreateService bookingCreateService;
    private final BookingCancelService bookingCancelService;

    @PostMapping
    public ResponseEntity<BookingConfirmationDto> createBooking(@RequestBody final BookingRequestDto bookingRequestDto) {
        return ResponseEntity.ok(bookingCreateService.createBooking(bookingRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable final UUID id) {
        bookingCancelService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
