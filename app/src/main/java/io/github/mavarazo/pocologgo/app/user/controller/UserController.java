package io.github.mavarazo.pocologgo.app.user.controller;

import io.github.mavarazo.pocologgo.app.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private static final Faker FAKER = new Faker();

    private final JmsClient jmsClient;

    @GetMapping
    public ResponseEntity<List<String>> getUsers() {

        final List<String> names = FAKER
                .collection(
                        () -> FAKER.name().firstName(),
                        () -> FAKER.name().lastName())
                .len(5, 10)
                .generate();

        log.info("Bingo");

        return ResponseEntity.ok(names);
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody final User user) {
        jmsClient.destination("user").send(user);
        return ResponseEntity.noContent().build();
    }
}
