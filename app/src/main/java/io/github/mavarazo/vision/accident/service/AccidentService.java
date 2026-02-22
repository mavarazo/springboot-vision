package io.github.mavarazo.vision.accident.service;

import io.github.mavarazo.vision.accident.model.Accident;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccidentService {

    public void processAccident(final Accident accident) {
        log.info("New accident '{}' for vehicle '{}' received", accident.id(), accident.vehicleId());
    }
}
