package io.github.mavarazo.vision.order.service;

import io.github.mavarazo.vision.order.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeliveryService {

    public void processOrder(final Order order) {
        log.info("Order '{}' send", order.item());
    }
}
