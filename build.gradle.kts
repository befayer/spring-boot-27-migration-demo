plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.kodacode"
version = "1.4.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Замена Sleuth: трассировка через Micrometer Tracing, провайдер Brave (совместим с B3 от gateway).
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("org.flywaydb:flyway-core")
    // Flyway 10+ вынес поддержку PostgreSQL в отдельный модуль.
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

// Контрактные тесты: внешний HTTP-клиент против уже запущенного сервиса.
// Они не зависят от Spring и не должны меняться при миграции.
val contractTest: SourceSet = sourceSets.create("contractTest")

// Только JDK HttpClient, Jackson, JDBC и JUnit: их API стабилен между версиями Boot,
// поэтому версии берутся из BOM, а код тестов при миграции не меняется.
dependencies {
    "contractTestImplementation"("com.fasterxml.jackson.core:jackson-databind")
    "contractTestImplementation"("org.postgresql:postgresql")
    "contractTestImplementation"("org.junit.jupiter:junit-jupiter")
    "contractTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.register<Test>("contractTest") {
    description = "Runs API contract tests against TARGET_BASE_URL."
    group = "verification"
    testClassesDirs = contractTest.output.classesDirs
    classpath = contractTest.runtimeClasspath
    useJUnitPlatform()
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = true
        showCauses = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    fun env(name: String, default: String) = providers.environmentVariable(name).orElse(default).get()
    systemProperty("targetBaseUrl", env("TARGET_BASE_URL", "http://localhost:8080"))
    systemProperty("dbUrl", env("DB_URL", "jdbc:postgresql://localhost:5432/billing"))
    systemProperty("dbUser", env("DB_USER", "billing"))
    systemProperty("dbPassword", env("DB_PASSWORD", "billing"))
}
