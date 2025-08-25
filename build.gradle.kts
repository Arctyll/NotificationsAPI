import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val modid: String by project
val transformerFile = file("src/main/resources/accesstransformer.cfg")
val lwjglVersion = "3.3.1"

group = baseGroup

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    withJavadocJar()
    withSourcesJar()
}

val shadowImpl by configurations.creating
configurations.implementation.get().extendsFrom(shadowImpl)

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        named("client") {
            arg("--tweakClass", "net.minecraftforge.fml.common.launcher.FMLTweaker")
        }
    }
    runConfigs {
        named("client") {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        if (transformerFile.exists()) {
            println("Installing access transformer")
            accessTransformer(transformerFile)
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net/")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    "minecraft"("com.mojang:minecraft:1.8.9")
    "mappings"("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    "forge"("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
    
    shadowImpl("org.reflections:reflections:0.10.2")

    shadowImpl("org.lwjgl:lwjgl:$lwjglVersion")
    shadowImpl("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    shadowImpl("org.lwjgl:lwjgl-nanovg:$lwjglVersion")

    listOf("natives-windows", "natives-linux", "natives-macos").forEach { classifier ->
        runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$classifier")
        runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$classifier")
        runtimeOnly("org.lwjgl:lwjgl-nanovg:$lwjglVersion:$classifier")
    }
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "net.minecraftforge.fml.common.launcher.FMLTweaker"
        if (transformerFile.exists())
            this["FMLAT"] = "${modid}_at.cfg"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)

    filesMatching("mcmod.info") {
        expand(inputs.properties)
    }

    rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)

    doLast {
        configurations.forEach {
            println("Shaded dependencies: ${it.files}")
        }
    }
}

tasks.assemble.get().dependsOn(tasks.remapJar)

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates("com.arctyll.notificationsapi", "notificationsapi", "0.1-alpha")

    pom {
        name.set("NotificationsAPI")
        description.set("An API to send notifications.")
        inceptionYear.set("2025")
        url.set("https://github.com/Arctyll/NotificationsAPI")
        licenses {
            license {
                name.set("The MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("classycoder")
                name.set("ClassyCoder")
                url.set("https://github.com/ClassyCoder1")
            }
        }
        scm {
            url.set("https://github.com/Arctyll/NotificationsAPI")
            connection.set("scm:git:git://github.com/Arctyll/NotificationsAPI.git")
            developerConnection.set("scm:git:ssh://git@github.com/Arctyll/NotificationsAPI.git")
        }
    }
}

tasks.matching { it.name.startsWith("generateMetadataFileFor") && it.name.endsWith("Publication") }
    .configureEach {
        dependsOn(tasks.named("plainJavadocJar"))
        dependsOn(tasks.named("sourcesJar"))
    }
