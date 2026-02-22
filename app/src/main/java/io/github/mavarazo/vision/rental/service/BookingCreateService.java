package io.github.mavarazo.vision.rental.service;

import io.github.mavarazo.vision.rental.exception.FraudException;
import io.github.mavarazo.vision.rental.model.Booking;
import io.github.mavarazo.vision.rental.model.BookingConfirmationDto;
import io.github.mavarazo.vision.rental.model.BookingRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingCreateService {

    private final JmsClient jmsClient;

    public BookingConfirmationDto createBooking(final BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.upto().isEqual(LocalDate.of(9999, 12, 31))) {
            throw FraudException.ofUnusualLongRentalTime();
        }

        final UUID vehicleId = bookingRequestDto.vehicleId();
        final LocalDate from = bookingRequestDto.from();
        final LocalDate upto = bookingRequestDto.upto();

        final Booking booking = new Booking(UUID.randomUUID(), vehicleId, UUID.randomUUID(), from, upto);
        jmsClient.destination("booking").send(booking);
        return new BookingConfirmationDto(booking.id(), booking.vehicleId(), booking.insuranceId(), booking.from(), booking.upto());
    }
}
