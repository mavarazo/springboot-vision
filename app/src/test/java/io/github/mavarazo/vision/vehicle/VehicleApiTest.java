package io.github.mavarazo.vision.vehicle;

import io.github.mavarazo.vision.vehicle.model.VehicleDto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
class VehicleApiTest {

    private static final String GET_VEHICLES_PATH = "/v1/vehicles";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Nested
    class GetVehiclesTests {

        @Test
        void status200() {
            // arrange
            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("user", "password");
            final RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(GET_VEHICLES_PATH));

            // act
            final ResponseEntity<VehicleDto[]> response = testRestTemplate.exchange(requestEntity, VehicleDto[].class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getHeaders())
                            .satisfies(h -> assertThat(h.get("x-correlation-id"))
                                    .isNotEmpty()
                            )
                    )
                    .satisfies(r -> assertThat(r.getBody())
                            .hasSize(25));
        }

        @Test
        void status200_with_external_correlation_id() {
            // arrange
            final HttpHeaders headers = new HttpHeaders();
            headers.add("X-Correlation-ID", "12345");
            headers.setBasicAuth("user", "password");
            final RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(GET_VEHICLES_PATH));

            // act
            final ResponseEntity<VehicleDto[]> response = testRestTemplate.exchange(requestEntity, VehicleDto[].class);

            // assert
            assertThat(response)
                    .satisfies(r -> assertThat(r.getHeaders())
                            .satisfies(h -> assertThat(h.get("x-correlation-id"))
                                    .singleElement()
                                    .isEqualTo("12345")
                            )
                    )
                    .satisfies(r -> assertThat(r.getBody())
                            .hasSize(25));
        }
    }
}