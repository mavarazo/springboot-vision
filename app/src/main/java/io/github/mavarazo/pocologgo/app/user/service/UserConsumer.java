package io.github.mavarazo.pocologgo.app.user.service;

import io.github.mavarazo.pocologgo.app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserConsumer {

    @JmsListener(destination = "${pocologgo.jms.queues.user}")
    public void listen(final User user) {
        log.info("Received user: {}", user);
    }
}
