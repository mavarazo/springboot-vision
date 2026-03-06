
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
    implementation(project(":shared-kafka"))
    implementation(project(":shared-persistence"))
    implementation(project(":shared-tracing"))

    // Testing
    implementation(project(":shared-testing"))

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    testImplementation("org.awaitility:awaitility:4.3.0")

    testRuntimeOnly("com.h2database:h2")
}

tasks.test {
    useJUnitPlatform()
}