plugins {
    java
}

sourceSets {
    main {
        output.setResourcesDir(file("build/classes/java/main"))
    }
}

group = "dev.mathops.web"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":mathops_db"))
    implementation(project(":mathops_dbjobs"))
    implementation(project(":mathops_assessment"))
    implementation(project(":mathops_session"))
    implementation(files("lib/mathops_commons.jar"))

    implementation("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0-M2")
    compileOnly("jakarta.websocket:jakarta.websocket-api:2.2.0")
    compileOnly("jakarta.websocket:jakarta.websocket-client-api:2.2.0")

    testImplementation(platform("org.junit:junit-bom:5.11.0-M1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0-M1")
}

tasks.test {
    useJUnitPlatform()
}
tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
    }
}
