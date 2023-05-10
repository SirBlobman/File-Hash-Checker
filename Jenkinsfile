pipeline {
    agent any

    options {
        githubProjectProperty(projectUrlStr: "https://github.com/SirBlobman/File-Hash-Checker")
    }

    triggers {
        githubPush()
    }

    tools {
        jdk "JDK 17"
    }

    stages {
        stage("Gradle: Build") {
            steps {
                withGradle {
                    sh("./gradlew --refresh-dependencies --no-daemon clean build")
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'build/libs/checker-*.jar', fingerprint: true
        }
    }
}
