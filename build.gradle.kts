plugins {
    id("java")
}

version="3.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    named<Jar>("jar") {
        manifest {
            attributes["Main-Class"] = "xyz.sirblobman.application.hash.FileHashChecker"
        }

        archiveBaseName.set("checker")
    }

    withType<JavaCompile>() {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
    }
}
