plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "org.medaware"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("anterogradia-maven") {
            from(components["kotlin"])
            artifactId = "anterogradia"
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(20)
}