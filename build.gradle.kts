import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val modid: String by project
val transformerFile = file("src/main/resources/accesstransformer.cfg")
val lwjglVersion = "3.3.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            arg("--tweakClass", "net.minecraftforge.fml.common.launcher.FMLTweaker")
        }
    }
    runConfigs {
        "client" {
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

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://maven.minecraftforge.net/")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

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

tasks.withType<Jar> {
    archiveBaseName.set(modid)
    manifest.attributes(
        "FMLCorePluginContainsFMLMod" to "true",
        "ForceLoadAsMod" to "true",
        "FMLAT" to if (transformerFile.exists()) "${modid}_at.cfg" else ""
    )
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
}

tasks.assemble.get().dependsOn(tasks.remapJar)

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = baseGroup
            artifactId = modid
            version = version

            from(components["java"])

            pom {
                name.set("Notifications API")
                description.set("A Minecraft modding API for rendering in-game notifications with auto-sizing, text wrapping, and NanoVG.")
                url.set("https://github.com/Arctyll/NotificationsAPI")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("arctyll")
                        name.set("Arctyll")
                        email.set("team@arctyll.com")
                    }
                }
                scm {
                    url.set("https://github.com/Arctyll/NotificationsAPI")
                    connection.set("scm:git:git://github.com/Arctyll/NotificationsAPI.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Arctyll/NotificationsAPI.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "Sonatype"
            url = uri(
                if (version.endsWith("SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: ""
                password = System.getenv("OSSRH_PASSWORD") ?: ""
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD") ?: ""
    )
    sign(publishing.publications["mavenJava"])
}

nexusPublishing {
    packageGroup.set(baseGroup)
    repositories {
        sonatype {
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}
