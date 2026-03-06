package io.github.mavarazo.app;

import io.github.mavarazo.vision.shared.persistence.SharedPersistenceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharedPersistenceConfig.class)
public class AppConfig {
}
