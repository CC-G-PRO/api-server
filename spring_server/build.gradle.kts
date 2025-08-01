plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"

    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.cc"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    // jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // h2db (for dev process)
    implementation("com.h2database:h2")

    // logger
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    //swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("org.apache.pdfbox:pdfbox:2.0.27") //pdf parsing 용

    //bootRun dev 환경에서만
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    // spring security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    //web client
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.0.1"))

    implementation("mysql:mysql-connector-java:8.0.33")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
