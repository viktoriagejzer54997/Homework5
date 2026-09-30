plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Source: https://mvnrepository.com/artifact/io.rest-assured/rest-assured
    testImplementation("io.rest-assured:rest-assured:5.5.6")
    testImplementation("org.assertj:assertj-core:3.26.3")
    implementation("net.datafaker:datafaker:2.2.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Test>("apiTest") {
    useJUnitPlatform()
    val testSourceSet = project.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("test")
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    filter {
        // Указываем конкретный класс. Если он лежит в пакете, укажите "com.example.Task2"
        includeTestsMatching("Task2")
    }
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }

}