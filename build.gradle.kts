plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    named<Jar>("jar") {
        archiveBaseName.set("checker")
        version = "3.0.0"

        manifest {
            attributes["Main-Class"] = "xyz.sirblobman.application.hash.FileHashChecker"
        }
    }

    withType<JavaCompile>() {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }
}
