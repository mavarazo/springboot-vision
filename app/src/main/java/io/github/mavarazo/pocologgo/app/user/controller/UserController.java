package io.github.mavarazo.pocologgo.app.user.controller;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@Slf4j
public class UserController {

    private static final Faker FAKER = new Faker();

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
}
