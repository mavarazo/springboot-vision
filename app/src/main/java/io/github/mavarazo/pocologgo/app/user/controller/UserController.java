package io.github.mavarazo.pocologgo.app.user.controller;

import io.github.mavarazo.pocologgo.app.ssn.service.SocialInsuranceAdapter;
import io.github.mavarazo.pocologgo.app.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private static final Faker FAKER = new Faker();

    private final SocialInsuranceAdapter socialInsuranceAdapter;
    private final JmsClient jmsClient;
    private final KafkaTemplate<String, User> kafkaTemplate;

    @GetMapping
    public ResponseEntity<List<String>> getUsers() {
        final List<String> names = FAKER
                .collection(
                        () -> FAKER.idNumber().ssnValid(),
                        () -> FAKER.name().firstName(),
                        () -> FAKER.name().lastName())
                .len(5, 10)
                .generate();
        return ResponseEntity.ok(names);
    }

    @PostMapping("/jms")
    public ResponseEntity<Void> createUser(@RequestBody final String ssn) {
        final User user = socialInsuranceAdapter.getUserBySocialInsuranceNo(ssn);

        jmsClient.destination("user").send(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/kafka")
    public ResponseEntity<Void> updateUser(@RequestBody final String ssn) {
        final User user = socialInsuranceAdapter.getUserBySocialInsuranceNo(ssn);

        kafkaTemplate.send("user", UUID.randomUUID().toString(), user)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka send success: {}", result.getRecordMetadata().offset());
                    }
                });

        return ResponseEntity.noContent().build();
    }
}
