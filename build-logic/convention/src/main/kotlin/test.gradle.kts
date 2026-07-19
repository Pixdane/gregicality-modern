import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
}

tasks.test {
    testLogging {
        events(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
    if (enableJUnit) useJUnitPlatform()
}

dependencies {
    if (enableJUnit) {
        testImplementation(platform(libs.junit.bom))
        testImplementation(libs.junit.jupiter)
        testImplementation(libs.munit)
        testRuntimeOnly(libs.junit.platform.launcher)
        testRuntimeOnly(libs.junit.vintage.engine)
    }
}
