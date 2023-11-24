plugins {
    id("java")
}

sourceSets {
    main {
        output.setResourcesDir(file("build/classes/java/main"))
    }
}

group = "dev.mathops.session"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("jakarta.websocket:jakarta.websocket-api:2.1.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":mathops_core"))
    implementation(project(":mathops_db"))
    implementation(project(":mathops_font"))
    implementation(project(":mathops_assessment"))
}

tasks.test {
    useJUnitPlatform()
}