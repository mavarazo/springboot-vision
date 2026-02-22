plugins {
    `java-library`
    id("org.springframework.boot") version "4.0.2" apply false
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
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-micrometer-tracing-brave")
    api("io.micrometer:micrometer-tracing-bridge-brave")

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
}

tasks.test {
    useJUnitPlatform()
}