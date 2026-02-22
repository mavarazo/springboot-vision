plugins {
    java
    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.mavarazo"
version = "0.0.1-SNAPSHOT"
description = "vision"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
