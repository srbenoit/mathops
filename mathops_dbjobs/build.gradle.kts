plugins {
    id("java")
}

sourceSets {
    main {
        output.setResourcesDir(file("build/classes/java/main"))
    }
}

group = "dev.mathops.dbjobs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":mathops_db"))
    implementation(files("../../mathops_commons/out/libs/mathops_commons.jar"))
    implementation(files("../../mathops_text/out/libs/mathops_text.jar"))

    implementation("com.ibm.informix:jdbc:4.50.10")
    implementation("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.formdev:flatlaf:3.4")

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