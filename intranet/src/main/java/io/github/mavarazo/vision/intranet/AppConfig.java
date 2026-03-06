package io.github.mavarazo.vision.intranet;

import io.github.mavarazo.vision.shared.persistence.SharedPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@Import(SharedPersistenceConfig.class)
@EnableAsync
@EnableKafka
public class AppConfig {
}
