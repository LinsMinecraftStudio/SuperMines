plugins {
    java
    id("com.diffplug.spotless") version "8.0.0"
}

group = "io.github.lijinhong11"
version = "1.6.0"

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

    compileOnly("com.google.guava:guava:33.5.0-jre")
    compileOnly("com.google.code.gson:gson:2.13.2")

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
