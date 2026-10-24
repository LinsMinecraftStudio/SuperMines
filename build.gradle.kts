import io.github.lijinhong11.nexusmcpublisher.VersionTag
import java.nio.charset.StandardCharsets

import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    java
    signing
    id("com.diffplug.spotless") version "8.9.0"
    id("io.github.lijinhong11.nexusmcpublisher") version "1.0.6"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.lijinhong11"
version = project.findProperty("version") as String

repositories {
    mavenCentral()
    maven("https://nexus.neetgames.com/repository/maven-releases/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.faststats.dev/releases")
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

    compileOnly("io.github.lijinhong11:MittelLib:1.4.8")
    compileOnly("io.github.lijinhong11:MDatabase:1.2.1")

    compileOnly("org.bstats:bstats-bukkit:3.2.1")
    compileOnly("dev.faststats.metrics:bukkit:0.30.2")

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

    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:3.2.0")

    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.2.049") {
        exclude(group = "com.google.guava")
    }
    compileOnly(files("libs/AuraSkills-2.4.0.jar"))

    compileOnly("io.github.lijinhong11:MittelRandomEvents:1.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.2")
    testImplementation("org.yaml:snakeyaml:2.5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.2")
}

tasks.test {
    useJUnitPlatform()
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

mavenPublishing {
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)

    signAllPublications()

    coordinates("io.github.lijinhong11", "SuperMines", project.version.toString())

    pom {
        name.set(project.name)
        description.set("Might be your best choice in mine plugins")
        url.set("https://github.com/LinsMinecraftStudio/MittelLib")

        licenses {
            license {
                name.set("GPL 3.0 License")
                url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
            }
        }

        developers {
            developer {
                id.set("lijinhong11")
                name.set("Jinhong Li")
                email.set("tygfhk@outlook.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/LinsMinecraftStudio/MittelLib.git")
            developerConnection.set("scm:git:ssh://github.com:LinsMinecraftStudio/SuperMines.git")
            url.set("https://github.com/LinsMinecraftStudio/MittelLib")
        }
    }

    configureBasedOnAppliedPlugins(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Javadoc(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources()
    )
}

signing {
    useGpgCmd()
}
