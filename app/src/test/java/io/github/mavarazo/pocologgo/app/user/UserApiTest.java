package io.github.mavarazo.pocologgo.app.user;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.mavarazo.pocologgo.app.user.model.User;
import io.github.mavarazo.pocologgo.app.user.service.UserConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
        partitions = 1,
        topics = {"user"}
)
@EnableWireMock(@ConfigureWireMock(
        baseUrlProperties = "pocologgo.social-insurance.endpoint"))
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

        @BeforeEach
        void setUp() {
            stubFor(get(urlPathTemplate("/v1/social-insurances/{ssn}"))
                    .willReturn(okForJson(new User("Foo", "Bingo", "123-45-6789"))));
        }

        @Test
        void status204_with_jms() {
            // arrange
            final String requestBody = "123-45-6789";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/jms"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );

            WireMock.verify(getRequestedFor(urlPathTemplate("/v1/social-insurances/{ssn}"))
                    .withHeader("X-Correlation-ID", matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$")));

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        final ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
                        verify(userConsumer).listenJms(userArgument.capture());
                        assertThat(userArgument.getValue())
                                .returns("Foo", User::firstName)
                                .returns("Bingo", User::lastName)
                                .returns("123-45-6789", User::ssn);
                    });
        }

        @Test
        void status204_with_jms_and_external_correlation_id() {
            // arrange
            final String requestBody = "123-45-6789";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            headers.add("X-Correlation-ID", "12345");
            final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/jms"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );


            WireMock.verify(getRequestedFor(urlPathTemplate("/v1/social-insurances/{ssn}"))
                    .withHeader("X-Correlation-ID", equalTo("12345")));

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        final ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
                        verify(userConsumer).listenJms(userArgument.capture());
                        assertThat(userArgument.getValue())
                                .returns("Foo", User::firstName)
                                .returns("Bingo", User::lastName)
                                .returns("123-45-6789", User::ssn);
                    });
        }

        @Test
        void status204_with_kafka() {
            // arrange
            final String requestBody = "123-45-6789";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/kafka"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );


            WireMock.verify(getRequestedFor(urlPathTemplate("/v1/social-insurances/{ssn}"))
                    .withHeader("X-Correlation-ID", matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$")));

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        final ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
                        verify(userConsumer).listenKafka(userArgument.capture());
                        assertThat(userArgument.getValue())
                                .returns("Foo", User::firstName)
                                .returns("Bingo", User::lastName)
                                .returns("123-45-6789", User::ssn);
                    });
        }

        @Test
        void status204_with_kafka_and_external_correlation_id() {
            // arrange
            final String requestBody = "123-45-6789";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            headers.add("X-Correlation-ID", "12345");
            final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/kafka"));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    );


            WireMock.verify(getRequestedFor(urlPathTemplate("/v1/social-insurances/{ssn}"))
                    .withHeader("X-Correlation-ID", WireMock.matching("12345")));

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        final ArgumentCaptor<User> userArgument = ArgumentCaptor.forClass(User.class);
                        verify(userConsumer).listenKafka(userArgument.capture());
                        assertThat(userArgument.getValue())
                                .returns("Foo", User::firstName)
                                .returns("Bingo", User::lastName)
                                .returns("123-45-6789", User::ssn);
                    });
        }
    }
}