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
    implementation(project(":mathops_assessment"))
    implementation(project(":mathops_session"))
    implementation(files("../../mathops_commons/out/libs/mathops_commons.jar"))
    implementation(files("../../mathops_text/out/libs/mathops_text.jar"))
    implementation(files("../../mathops_db/out/libs/mathops_db.jar"))

    implementation("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    compileOnly("jakarta.websocket:jakarta.websocket-api:2.2.0")
    compileOnly("jakarta.websocket:jakarta.websocket-client-api:2.2.0")

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}
tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
    }
}
