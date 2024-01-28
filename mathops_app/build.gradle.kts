plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

sourceSets {
    main {
        output.setResourcesDir(file("build/classes/java/main"))
    }
}

group = "dev.mathops.app"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":mathops_db"))
    implementation(project(":mathops_dbjobs"))
    implementation(project(":mathops_font"))
    implementation(project(":mathops_assessment"))
    implementation(project(":mathops_session"))
    implementation(files("lib/jwabbit.jar"))
    implementation(files("lib/mathops_commons.jar"))
    implementation(files("lib/mathops_persistence.jar"))

    implementation("org.openjfx:javafx:21.0.1")
    implementation("org.openjfx:javafx-controls:21.0.1")

    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
    implementation("com.formdev:flatlaf:3.2.5")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "21.0.1"
    modules("javafx.controls", "javafx.fxml")
}