plugins {
    id("java")
}

group = "dev.mathops.db"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation(project(":mathops_core"))
}

tasks.test {
    useJUnitPlatform()
}