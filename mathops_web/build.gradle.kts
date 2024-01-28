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

    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("jakarta.websocket:jakarta.websocket-api:2.1.1")
    compileOnly("jakarta.websocket:jakarta.websocket-client-api:2.1.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}
