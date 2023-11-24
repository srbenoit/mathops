plugins {
    id("java")
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
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":mathops_core"))
    implementation(project(":mathops_db"))
    implementation(project(":mathops_dbjobs"))
    implementation(project(":mathops_font"))
    implementation(project(":mathops_assessment"))
    implementation(project(":mathops_session"))
    implementation(project(":jwabbit"))

    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")

    implementation("com.formdev:flatlaf:3.2.5")
}

tasks.test {
    useJUnitPlatform()
}