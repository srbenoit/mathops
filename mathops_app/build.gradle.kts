plugins {
    id("java")
}

group = "dev.mathops.app"
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
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":mathops_core"))
    implementation(project(":mathops_db"))
    implementation(project(":mathops_dbjobs"))
    implementation(project(":mathops_font"))
    implementation(project(":mathops_assessment"))
    implementation(project(":mathops_session"))
    implementation(project(":jwabbit"))

    // https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc11
    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")

    // https://mvnrepository.com/artifact/com.formdev/flatlaf
    implementation("com.formdev:flatlaf:3.2.5")

}

tasks.test {
    useJUnitPlatform()
}