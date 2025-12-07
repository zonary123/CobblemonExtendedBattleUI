plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "2.2.0"
}

version = property("mod_version")!!
group = property("maven_group")!!

base {
    archivesName.set(property("archives_base_name").toString())
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    // Cobblemon maven
    maven("https://maven.impactdev.net/repository/development/")
    // GeckoLib for animations
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    // Architectury
    maven("https://maven.architectury.dev/")
    // Shedaniel (for cloth-config if needed)
    maven("https://maven.shedaniel.me/")
}

dependencies {
    // Minecraft & Fabric
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    // Cobblemon - this is the mod we're extending
    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

// Configure Loom
loom {
    accessWidenerPath.set(file("src/main/resources/cobblemonbattleui.accesswidener"))

    mixin {
        defaultRefmapName.set("cobblemonbattleui-refmap.json")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        // Add repositories for publishing here
    }
}
