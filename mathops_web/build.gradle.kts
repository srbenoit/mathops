plugins {
    id("java")
}

group = "dev.mathops.web"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    compileOnly(files("../jars//javax.servlet-api.jar"))
    compileOnly("javax.websocket:javax.websocket-api:1.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation(project(":mathops_core"))
    implementation(project(":mathops_db"))
    implementation(project(":mathops_dbjobs"))
    implementation(project(":mathops_assessment"))
    implementation(project(":mathops_session"))

    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
}

tasks.test {
    useJUnitPlatform()
}