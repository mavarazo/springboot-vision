package io.github.mavarazo.vision.user;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.mavarazo.vision.user.model.User;
import io.github.mavarazo.vision.user.service.UserConsumer;
import org.assertj.core.api.InstanceOfAssertFactories;
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
import org.springframework.http.ProblemDetail;
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
        baseUrlProperties = "vision.social-insurance.endpoint"))
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
                            .satisfies(h -> assertThat(h.get("x-correlation-id"))
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
                            .satisfies(h -> assertThat(h.get("x-correlation-id"))
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

        @Nested
        class JmsTests {

            @Test
            void status204() {
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
                        .withHeader("x-correlation-id", matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$")));

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
            void status204_with_external_correlation_id() {
                // arrange
                final String requestBody = "123-45-6789";

                final HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth("user", "password");
                headers.add("x-correlation-id", "12345");
                final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/jms"));

                // act
                final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

                // assert
                assertThat(response)
                        .satisfies(r -> assertThat(r.getStatusCode())
                                .isEqualTo(HttpStatus.NO_CONTENT)
                        );


                WireMock.verify(getRequestedFor(urlPathTemplate("/v1/social-insurances/{ssn}"))
                        .withHeader("x-correlation-id", equalTo("12345")));

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
            void status404() {
                // arrange
                stubFor(get(urlPathTemplate("/v1/social-insurances/{ssn}"))
                        .willReturn(WireMock.notFound()));

                final String requestBody = "123-45-6789";

                final HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth("user", "password");
                headers.add("x-correlation-id", "12345");
                final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/jms"));

                // act
                final ResponseEntity<ProblemDetail> response = testRestTemplate.exchange(requestEntity, ProblemDetail.class);

                // assert
                assertThat(response)
                        .satisfies(r -> assertThat(r.getStatusCode())
                                .isEqualTo(HttpStatus.NOT_FOUND)
                        )
                        .satisfies(r -> assertThat(r.getBody())
                                .returns(HttpStatus.NOT_FOUND.value(), ProblemDetail::getStatus)
                                .returns("Not Found", ProblemDetail::getTitle)
                                .returns("user.not-found", ProblemDetail::getDetail)
                                .doesNotReturn(null, p -> p.getProperties().get("trace.id"))
                                .returns("12345", p -> p.getProperties().get("correlation.id"))
                                .satisfies(p -> assertThat(p.getProperties().get("parameters"))
                                        .asInstanceOf(InstanceOfAssertFactories.MAP)
                                        .hasEntrySatisfying("ssn", v -> assertThat(v).isEqualTo("123-45-6789"))
                                )
                        );
            }

            @Test
            void status503() {
                // arrange
                stubFor(get(urlPathTemplate("/v1/social-insurances/{ssn}"))
                        .willReturn(WireMock.serverError()));

                final String requestBody = "123-45-6789";

                final HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth("user", "password");
                headers.add("x-correlation-id", "12345");
                final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/jms"));

                // act
                final ResponseEntity<ProblemDetail> response = testRestTemplate.exchange(requestEntity, ProblemDetail.class);

                // assert
                assertThat(response)
                        .satisfies(r -> assertThat(r.getStatusCode())
                                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                        )
                        .satisfies(r -> assertThat(r.getBody())
                                .returns(HttpStatus.SERVICE_UNAVAILABLE.value(), ProblemDetail::getStatus)
                                .returns("Service Unavailable", ProblemDetail::getTitle)
                                .returns("user.ssn.service-unavailable", ProblemDetail::getDetail)
                                .doesNotReturn(null, p -> p.getProperties().get("trace.id"))
                                .returns("12345", p -> p.getProperties().get("correlation.id"))
                        );
            }
        }

        @Nested
        class KafkaTests {

            @Test
            void status204() {
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
                        .withHeader("x-correlation-id", matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$")));

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
            void status204_with_external_correlation_id() {
                // arrange
                final String requestBody = "123-45-6789";

                final HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth("user", "password");
                headers.add("x-correlation-id", "12345");
                final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/users/kafka"));

                // act
                final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

                // assert
                assertThat(response)
                        .satisfies(r -> assertThat(r.getStatusCode())
                                .isEqualTo(HttpStatus.NO_CONTENT)
                        );


                WireMock.verify(getRequestedFor(urlPathTemplate("/v1/social-insurances/{ssn}"))
                        .withHeader("x-correlation-id", WireMock.matching("12345")));

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
}