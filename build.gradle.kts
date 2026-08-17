import io.github.lijinhong11.nexusmcpublisher.VersionTag
import java.nio.charset.StandardCharsets

plugins {
    java
    id("com.diffplug.spotless") version "8.0.0"
    id("io.github.lijinhong11.nexusmcpublisher") version "1.0.3"
}

group = "io.github.lijinhong11"
version = "1.6.3"

repositories {
    mavenCentral()
    maven("https://nexus.neetgames.com/repository/maven-releases/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    compileOnly("com.google.code.gson:gson:2.14.0") {
        version { strictly("2.14.0") }
    }
    compileOnly("it.unimi.dsi:fastutil:8.5.16") {
        version { strictly("8.5.16") }
    }
    compileOnly("com.google.guava:guava:33.5.0-jre") {
        version { strictly("33.5.0-jre") }
    }

    compileOnly("io.github.lijinhong11:MittelLib:1.2.7")
    compileOnly("io.github.lijinhong11:MDatabase:1.2.0")
    compileOnly("org.bstats:bstats-bukkit:3.2.1")

    compileOnly("dev.jorel:commandapi-bukkit-core:12.0.0")
    compileOnly("com.mojang:brigadier:1.0.18")

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.1") {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.1") {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
        exclude(group = "it.unimi.dsi")
    }

    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.3.0")

    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.2.045") {
        exclude(group = "com.google.guava")
    }

    compileOnly(files("libs/AuraSkills-2.3.9.jar"))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        palantirJavaFormat()
        formatAnnotations()
    }
}

nexusMCPublisher {
    resourceId.set("56db359b-d055-42ae-93c2-6a71b43ba0b3")
    versionTag.set(VersionTag.RELEASE)
    versionTitle = project.property("version") as String
    changelog.set(file("changelog.txt").readLines(StandardCharsets.UTF_8).joinToString("\n"))
    mcVersions.set(listOf("26.1", "26.1.1", "26.1.2", "26.2"))
    token = System.getenv("NEXUSMC_API_TOKEN")
}
