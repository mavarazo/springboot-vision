package io.github.mavarazo.vision.rental.service;

import io.github.mavarazo.vision.insurance.service.InsuranceAdapter;
import io.github.mavarazo.vision.rental.exception.NotFoundException;
import io.github.mavarazo.vision.rental.exception.UnprocessableContentException;
import io.github.mavarazo.vision.rental.model.RentalMessage;
import io.github.mavarazo.vision.shared.exception.TechnicalException;
import io.github.mavarazo.vision.shared.persistence.entity.Rental;
import io.github.mavarazo.vision.shared.persistence.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetnalCancelService {

    private final RentalRepository rentalRepository;
    private final InsuranceAdapter insuranceAdapter;
    private final KafkaTemplate<String, RentalMessage> rentalKafkaTemplate;

    public void cancelRental(final UUID id) {
        final Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> NotFoundException.ofRental(id));

        cancelInsurance(rental.getInsuranceId());

        rentalKafkaTemplate.send("rental", rental.getId().toString(), null)
                .whenComplete((_, ex) -> {
                    if (ex == null) {
                        log.info("Kafka send success");
                    }
                });
    }

    private void cancelInsurance(final UUID insuranceId) {
        try {
            insuranceAdapter.deleteInsurance(insuranceId);
        } catch (final HttpClientErrorException ex) {
            throw UnprocessableContentException.ofInsurance(insuranceId);
        } catch (final HttpServerErrorException ex) {
            throw new TechnicalException(HttpStatus.SERVICE_UNAVAILABLE, "insurance.service-unavailable", ex);
        }
    }
}
