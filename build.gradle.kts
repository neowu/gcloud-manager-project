plugins {
    java
    application
}

apply(plugin = "project")

subprojects {
    group = "core.infrastructure"
    version = "0.0.5"
}

val jacksonVersion = "2.15.2"
val junitVersion = "5.10.0"
val mockitoVersion = "5.4.0"
val assertjVersion = "3.24.2"
val mysqlVersion = "8.2.0"

project(":gcloud-manager") {
    apply(plugin = "app")

    application {
        applicationName = "gm"
    }

    dependencies {
        implementation("ch.qos.logback:logback-classic:1.4.14")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${jacksonVersion}")

        runtimeOnly("com.mysql:mysql-connector-j:${mysqlVersion}")

        testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        testImplementation("org.mockito:mockito-junit-jupiter:${mockitoVersion}")
        testImplementation("org.assertj:assertj-core:${assertjVersion}")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}
