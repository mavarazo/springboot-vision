package io.github.mavarazo.vision.insurance.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.DeleteExchange;

import java.util.UUID;

public interface InsuranceAdapter {

    @DeleteExchange("/v1/insurances/{id}")
    void deleteInsurance(@PathVariable UUID id);
}
