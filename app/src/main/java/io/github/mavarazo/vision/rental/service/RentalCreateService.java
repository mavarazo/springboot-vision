package io.github.mavarazo.vision.rental.service;

import io.github.mavarazo.vision.rental.exception.FraudException;
import io.github.mavarazo.vision.rental.model.RentalConfirmationDto;
import io.github.mavarazo.vision.rental.model.RentalMessage;
import io.github.mavarazo.vision.rental.model.RentalRequestDto;
import io.github.mavarazo.vision.shared.persistence.entity.Rental;
import io.github.mavarazo.vision.shared.persistence.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentalCreateService {

    private final RentalRepository rentalRepository;
    private final JmsClient jmsClient;

    public RentalConfirmationDto createRental(final RentalRequestDto rentalRequestDto) {
        if (rentalRequestDto.upto().isEqual(LocalDate.of(9999, 12, 31))) {
            throw FraudException.ofUnusualLongRentalTime();
        }

        final UUID vehicleId = rentalRequestDto.vehicleId();
        final LocalDate from = rentalRequestDto.from();
        final LocalDate upto = rentalRequestDto.upto();

        final Rental rental = rentalRepository.save(Rental.builder()
                .vehicleId(vehicleId)
                .insuranceId(UUID.randomUUID())
                .from(from)
                .upto(upto)
                .build()
        );

        jmsClient.destination("rental").send(new RentalMessage(rental.getId(), rental.getVehicleId(), rental.getInsuranceId(), rental.getFrom(), rental.getUpto()));
        return new RentalConfirmationDto(rental.getId(), rental.getVehicleId(), rental.getInsuranceId(), rental.getFrom(), rental.getUpto());
    }
}
