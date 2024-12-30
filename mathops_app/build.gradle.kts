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
    implementation(files("../../mathops_commons/out/libs/mathops_commons.jar"))
    implementation(files("../../mathops_text/out/libs/mathops_text.jar"))
    implementation(files("../../mathops_persistence/out/libs/mathops_persistence.jar"))
    implementation(files("../../JWabbit/out/libs/jwabbit_jar/jwabbit.jar"))

    implementation("org.openjfx:javafx:23-ea+20")
    implementation("org.openjfx:javafx-controls:23-ea+22")

    implementation("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")
    implementation("com.formdev:flatlaf:3.4")
    implementation("org.postgresql:postgresql:42.7.4")

    testImplementation(platform("org.junit:junit-bom:5.11.0-M1"))
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

javafx {
    version = "21.0.1"
    modules("javafx.controls", "javafx.fxml")
}