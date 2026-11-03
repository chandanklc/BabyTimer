// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "7.5.0.8588"
}

sonar {
    properties {
        property("sonar.projectKey", "BabyTimer")
        property("sonar.projectName", "BabyTimer")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "app/src/main/java")
        property("sonar.tests", "app/src/test/java")
    }
}
