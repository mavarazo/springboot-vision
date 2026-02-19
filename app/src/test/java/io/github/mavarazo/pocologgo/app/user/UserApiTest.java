package io.github.mavarazo.pocologgo.app.user;

import io.github.mavarazo.pocologgo.app.user.model.User;
import io.github.mavarazo.pocologgo.app.user.service.UserConsumer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
        partitions = 1,
        topics = {"user"}
)
@ActiveProfiles("test")
class UserApiTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoSpyBean
    private UserConsumer userConsumer;

    @Nested
    class GetUsersTests {

        @Test
        void status200() {
            // arrange
            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            final RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create("/v1/users"));

            // act
            final ResponseEntity<String[]> response = testRestTemplate.exchange(requestEntity, String[].class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getHeaders())
                            .satisfies(h -> assertThat(h.get("X-Correlation-ID"))
                                    .isNotEmpty()
                            )
                    )
                    .satisfies(r -> assertThat(r.getBody())
                            .hasSizeBetween(5, 15));
        }

        @Test
        void status200_with_external_correlation_id() {
            // arrange
            final HttpHeaders headers = new HttpHeaders();
            headers.add("X-Correlation-ID", "12345");
            headers.setBasicAuth("user", "password");
            final RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create("/v1/users"));

            // act
            final ResponseEntity<String[]> response = testRestTemplate.exchange(requestEntity, String[].class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getHeaders())
                            .satisfies(h -> assertThat(h.get("X-Correlation-ID"))
                                    .singleElement()
                                    .isEqualTo("12345")
                            )
                    )
                    .satisfies(r -> assertThat(r.getBody())
                            .hasSizeBetween(5, 15));
        }
    }

    @Nested
    class CreateUserTests {

        @Test
        void status204_with_jms() {
            // arrange
            final User requestBody = new User("Bingo", "Foo");

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            final RequestEntity<User> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/jms"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(userConsumer).listenJms(requestBody);
                    });
        }

        @Test
        void status204_with_jms_and_external_correlation_id() {
            // arrange
            final User requestBody = new User("Bingo", "Foo");

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            headers.add("X-Correlation-ID", "12345");
            final RequestEntity<User> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/jms"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(userConsumer).listenJms(requestBody);
                    });
        }

        @Test
        void status204_with_kafka() {
            // arrange
            final User requestBody = new User("Bingo", "Foo");

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            final RequestEntity<User> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/kafka"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(userConsumer).listenKafka(requestBody);
                    });
        }

        @Test
        void status204_with_kafka_and_external_correlation_id() {
            // arrange
            final User requestBody = new User("Bingo", "Foo");

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            headers.add("X-Correlation-ID", "12345");
            final RequestEntity<User> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/kafka"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(userConsumer).listenKafka(requestBody);
                    });
        }
    }
}