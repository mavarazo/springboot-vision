package io.github.mavarazo.vision.rental.service;

import io.github.mavarazo.vision.insurance.service.InsuranceAdapter;
import io.github.mavarazo.vision.rental.exception.NotFoundException;
import io.github.mavarazo.vision.rental.model.Booking;
import io.github.mavarazo.vision.shared.exception.TechnicalException;
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
public class BookingCancelService {

    private final InsuranceAdapter insuranceAdapter;
    private final KafkaTemplate<String, Booking> orderKafkaTemplate;

    public void cancelBooking(final UUID id) {
        cancelInsurance(id);
        orderKafkaTemplate.send("booking", id.toString(), null)
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
            throw NotFoundException.ofInsurance(insuranceId);
        } catch (final HttpServerErrorException ex) {
            throw new TechnicalException(HttpStatus.SERVICE_UNAVAILABLE, "insurance.service-unavailable", ex);
        }
    }
}
