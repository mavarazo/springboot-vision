package io.github.mavarazo.app.rental.service;

import io.github.mavarazo.app.insurance.service.InsuranceAdapter;
import io.github.mavarazo.app.rental.exception.NotFoundException;
import io.github.mavarazo.app.rental.exception.UnprocessableContentException;
import io.github.mavarazo.vision.shared.exception.TechnicalException;
import io.github.mavarazo.vision.shared.persistence.entity.Rental;
import io.github.mavarazo.vision.shared.persistence.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalCancelService {

    private final RentalRepository rentalRepository;
    private final KafkaMessageService kafkaMessageService;
    private final InsuranceAdapter insuranceAdapter;

    public void cancelRental(final UUID id) {
        final Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> NotFoundException.ofRental(id));

        cancelInsurance(rental.getInsuranceId());
        kafkaMessageService.saveMessage("rental", rental.getId().toString(), null);
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
