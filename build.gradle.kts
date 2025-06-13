plugins {
    id("java")
}

group = "dev.mathops"
version = "1.0-SNAPSHOT"

sourceSets {
    main {
        output.setResourcesDir(file("build/classes/java/main"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.ibm.informix:jdbc:4.50.10")
    implementation("com.oracle.database.jdbc:ojdbc11:23.4.0.24.05")
    implementation("org.postgresql:postgresql:42.7.4")

    testImplementation(platform("org.junit:junit-bom:5.11.0-M1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.postgresql:postgresql:42.7.4")
}

tasks.test {
    useJUnitPlatform()
}

allprojects {
    tasks.withType<JavaCompile> {
        val compilerArgs = options.compilerArgs
        compilerArgs.add("-Xlint:unchecked")
        compilerArgs.add("-Xlint:deprecation")
    }
}