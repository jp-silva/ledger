plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.5.0"
	id("com.diffplug.spotless") version "8.0.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "API to interact with a ledger"

val mapstructVersion = "1.6.3"

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
	implementation("org.mapstruct:mapstruct:${mapstructVersion}")
	// OpenAPI/Swagger dependencies
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
	implementation("org.openapitools:jackson-databind-nullable:0.2.7")

	annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
	main {
		java {
			srcDir("${buildDir}/generate-resources/main/src/main/java")
		}
	}
}

tasks.getByName("compileJava") {
	dependsOn("openApiGenerate")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

openApiGenerate {
	// Path to your OpenAPI specification file
	inputSpec.set("$projectDir/openapi/api-spec.yml")

	// Generator to use
	generatorName.set("spring")

	// Package naming for generated classes
	apiPackage.set("com.example.ledger.generated.api")
	modelPackage.set("com.example.ledger.generated.model")

	// Configuration options
	configOptions.set(mapOf(
		"interfaceOnly" to "true",
		"useSpringBoot3" to "true",
		"useTags" to "true"
	))
}

spotless {
	java {
		googleJavaFormat()
		target("src/main/java/**/*.java")
	}
}