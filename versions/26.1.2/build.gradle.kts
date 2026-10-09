plugins {
    java
}

group = "com.strikesstg"
version = "1.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("StrikesSTG")
    archiveClassifier.set("26.1.2")
}
