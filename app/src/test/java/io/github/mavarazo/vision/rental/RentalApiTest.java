package io.github.mavarazo.vision.rental;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.mavarazo.vision.rental.model.Booking;
import io.github.mavarazo.vision.rental.model.BookingConfirmationDto;
import io.github.mavarazo.vision.rental.model.BookingRequestDto;
import io.github.mavarazo.vision.rental.service.BookingConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
        partitions = 1,
        topics = {"booking"}
)
@EnableWireMock(
        @ConfigureWireMock(baseUrlProperties = "vision.insurance.endpoint")
)
class RentalApiTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoSpyBean
    private BookingConsumer bookingConsumer;

    @Nested
    class CreateBookingTests {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = "56750ce5-0e45-4664-9caa-fc0fe1d5b4c9")
        void status204(final String correlationId) {
            // arrange
            final BookingRequestDto requestBody = new BookingRequestDto(UUID.randomUUID(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            if (correlationId != null) {
                headers.add("x-correlation-id", correlationId);
            }
            final RequestEntity<BookingRequestDto> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/rentals"));

            // act
            final ResponseEntity<BookingConfirmationDto> response = testRestTemplate.exchange(requestEntity, BookingConfirmationDto.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.OK)
                    )
                    .satisfies(r -> assertThat(r.getHeaders().get("x-correlation-id"))
                            .isNotEmpty()
                    );

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        final ArgumentCaptor<Booking> userArgument = ArgumentCaptor.forClass(Booking.class);
                        verify(bookingConsumer).listenJms(userArgument.capture());
                        assertThat(userArgument.getValue()).isNotNull();
                    });
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = "56750ce5-0e45-4664-9caa-fc0fe1d5b4c9")
        void status422(final String correlationId) {
            // arrange
            final BookingRequestDto requestBody = new BookingRequestDto(UUID.randomUUID(), LocalDate.of(2026, 1, 1), LocalDate.of(9999, 12, 31));

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            if (correlationId != null) {
                headers.add("x-correlation-id", correlationId);
            }
            final RequestEntity<BookingRequestDto> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.POST, URI.create("/v1/rentals"));

            // act
            final ResponseEntity<ProblemDetail> response = testRestTemplate.exchange(requestEntity, ProblemDetail.class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                    )
                    .satisfies(r -> assertThat(r.getHeaders().get("x-correlation-id"))
                            .isNotEmpty()
                    )
                    .satisfies(r -> assertThat(r.getBody())
                            .isNotNull()
                            .returns(422, ProblemDetail::getStatus)
                            .returns("Unprocessable Content", ProblemDetail::getTitle)
                            .returns("rental.fraud-detection", ProblemDetail::getDetail)
                            .doesNotReturn(null, p -> p.getProperties().get("trace.id"))
                            .doesNotReturn(null, p -> p.getProperties().get("correlation.id"))
                    );
        }
    }

    @Nested
    class DeleteBookingTests {

        @BeforeEach
        void setUp() {
            WireMock.stubFor(WireMock.delete(WireMock.urlPathTemplate("/v1/insurances/{id}"))
                    .willReturn(WireMock.ok()));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = "56750ce5-0e45-4664-9caa-fc0fe1d5b4c9")
        void status200(final String correlationId) {
            // arrange
            final String requestBody = "123-45-6789";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            if (correlationId != null) {
                headers.add("x-correlation-id", correlationId);
            }
            final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.DELETE, URI.create("/v1/rentals/" + UUID.randomUUID()));

            // act
            final ResponseEntity<Void> response = testRestTemplate.exchange(requestEntity, Void.class);

            // assert
            WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathTemplate("/v1/insurances/{id}"))
                    .withHeader("x-correlation-id", WireMock.matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$")));

            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.NO_CONTENT)
                    )
                    .satisfies(r -> assertThat(r.getHeaders().get("x-correlation-id"))
                            .isNotEmpty()
                    );

            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        final ArgumentCaptor<ConsumerRecord<String, Booking>> recordArgument = ArgumentCaptor.forClass(ConsumerRecord.class);
                        verify(bookingConsumer).listenKafka(recordArgument.capture());
                        assertThat(recordArgument.getValue())
                                .returns(null, ConsumerRecord::value);
                    });
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = "56750ce5-0e45-4664-9caa-fc0fe1d5b4c9")
        void status422(final String correlationId) {
            // arrange
            WireMock.stubFor(WireMock.delete(WireMock.urlPathTemplate("/v1/insurances/{id}"))
                    .willReturn(WireMock.notFound()));

            final String requestBody = "123-45-6789";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            if (correlationId != null) {
                headers.add("x-correlation-id", correlationId);
            }
            final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.DELETE, URI.create("/v1/rentals/" + UUID.randomUUID()));

            // act
            final ResponseEntity<ProblemDetail> response = testRestTemplate.exchange(requestEntity, ProblemDetail.class);

            // assert
            WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathTemplate("/v1/insurances/{id}"))
                    .withHeader("x-correlation-id", WireMock.matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$")));

            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                    )
                    .satisfies(r -> assertThat(r.getHeaders().get("x-correlation-id"))
                            .isNotEmpty()
                    )
                    .satisfies(r -> assertThat(r.getBody())
                            .isNotNull()
                            .returns(422, ProblemDetail::getStatus)
                            .returns("Unprocessable Content", ProblemDetail::getTitle)
                            .returns("insurance.not-found", ProblemDetail::getDetail)
                            .doesNotReturn(null, p -> p.getProperties().get("trace.id"))
                            .doesNotReturn(null, p -> p.getProperties().get("correlation.id"))
                    );
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = "56750ce5-0e45-4664-9caa-fc0fe1d5b4c9")
        void status503(final String correlationId) {
            // arrange
            WireMock.stubFor(WireMock.delete(WireMock.urlPathTemplate("/v1/insurances/{id}"))
                    .willReturn(WireMock.serverError()));

            final String requestBody = "123-45-6789";

            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            if (correlationId != null) {
                headers.add("x-correlation-id", correlationId);
            }
            final RequestEntity<String> requestEntity = new RequestEntity<>(requestBody, headers, HttpMethod.DELETE, URI.create("/v1/rentals/" + UUID.randomUUID()));

            // act
            final ResponseEntity<ProblemDetail> response = testRestTemplate.exchange(requestEntity, ProblemDetail.class);

            // assert
            WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathTemplate("/v1/insurances/{id}"))
                    .withHeader("x-correlation-id", WireMock.matching("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$")));

            assertThat(response)
                    .satisfies(r -> assertThat(r.getStatusCode())
                            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                    )
                    .satisfies(r -> assertThat(r.getHeaders().get("x-correlation-id"))
                            .isNotEmpty()
                    )
                    .satisfies(r -> assertThat(r.getBody())
                            .isNotNull()
                            .returns(503, ProblemDetail::getStatus)
                            .returns("Service Unavailable", ProblemDetail::getTitle)
                            .returns("insurance.service-unavailable", ProblemDetail::getDetail)
                            .doesNotReturn(null, p -> p.getProperties().get("trace.id"))
                            .doesNotReturn(null, p -> p.getProperties().get("correlation.id"))
                    );
        }
    }
}