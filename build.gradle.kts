import org.apache.commons.lang3.SystemUtils
import org.jreleaser.model.Active

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
    id("org.jreleaser") version "1.9.0"
}

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val modid: String by project
val transformerFile = file("src/main/resources/accesstransformer.cfg")
val lwjglVersion = "3.3.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    withJavadocJar()
    withSourcesJar()
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        create("client") {
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

val shadowImpl by configurations.creating {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    "minecraft"("com.mojang:minecraft:1.8.9")
    "mappings"("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    "forge"("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowImpl("org.lwjgl:lwjgl:$lwjglVersion")
    shadowImpl("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    shadowImpl("org.lwjgl:lwjgl-nanovg:$lwjglVersion")

    listOf("natives-windows", "natives-linux", "natives-macos").forEach { classifier ->
        runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$classifier")
        runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$classifier")
        runtimeOnly("org.lwjgl:lwjgl-nanovg:$lwjglVersion:$classifier")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Jar>().configureEach {
    archiveBaseName.set(modid)
    manifest.attributes(
        "FMLCorePluginContainsFMLMod" to "true",
        "ForceLoadAsMod" to "true",
        "FMLAT" to if (transformerFile.exists()) "${modid}_at.cfg" else ""
    )
}

tasks.processResources {
    inputs.properties(mapOf(
        "version" to project.version,
        "mcversion" to mcVersion,
        "modid" to modid,
        "basePackage" to baseGroup
    ))
    filesMatching("mcmod.info") {
        expand(inputs.properties)
    }
    rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
}

val remapJar by tasks.named<RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.named<ShadowJar>("shadowJar"))
    input.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("non-obfuscated-with-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    configurations = listOf(shadowImpl)
}

tasks.assemble {
    dependsOn(remapJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = baseGroup
            artifactId = "notificationsapi"
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set("NotificationsAPI")
                description.set("Notifications API")
                url.set("https://github.com/Arctyll/NotificationsAPI")
                inceptionYear.set("2025")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/license/mit")
                    }
                }
                developers {
                    developer {
                        id.set("aalmiray")
                        name.set("Andres Almiray")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Arctyll/NotificationsAPI.git")
                    developerConnection.set("scm:git:ssh://github.com/Arctyll/NotificationsAPI.git")
                    url.set("https://github.com/Arctyll/NotificationsAPI")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            mavenCentral {
                active.set(Active.ALWAYS)
                targetUrl.set("https://central.sonatype.com/api/v1/publisher")
                stagingRepository.set(layout.buildDirectory.dir("staging-deploy").get().asFile)
            }
        }
    }
}
