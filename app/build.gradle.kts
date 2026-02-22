
plugins {
    java
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    // Lombok
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    // Core
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation(project(":shared-exception"))
    implementation(project(":shared-persistence"))
    implementation(project(":shared-tracing"))

    implementation("org.springframework.boot:spring-boot-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("net.datafaker:datafaker:2.5.4")

    // JMS
    implementation("org.springframework.boot:spring-boot-starter-artemis")
    runtimeOnly("org.apache.activemq:artemis-jakarta-server")

    // Kafka
    implementation("org.springframework.boot:spring-boot-starter-kafka")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation("org.wiremock.integrations:wiremock-spring-boot:4.0.9")

    testRuntimeOnly("com.h2database:h2")
}

tasks.test {
    useJUnitPlatform()
}