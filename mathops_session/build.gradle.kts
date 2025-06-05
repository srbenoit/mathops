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
    implementation(project(":mathops_assessment"))
    implementation(files("../../mathops_commons/out/libs/mathops_commons.jar"))
    implementation(files("../../mathops_text/out/libs/mathops_text.jar"))
    implementation(files("../../mathops_db/out/libs/mathops_db.jar"))

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0-M2")
    compileOnly("jakarta.websocket:jakarta.websocket-api:2.2.0")

    testImplementation(platform("org.junit:junit-bom:5.11.0-M1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
    }
}