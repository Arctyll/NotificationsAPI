plugins {
    java
    id("maven-publish")
    id("signing")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.arctyll"
version = "1.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

repositories {
    mavenCentral()
    maven { url = uri("https://files.minecraftforge.net/maven") }
}

dependencies {
    compileOnly("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "FMLCorePlugin" to "com.arctyll.notificationsapi.core.NotificationsCoremod",
            "FMLCorePluginContainsMod" to "true",
            "TweakOrder" to "0",
            "ForceLoadAsMod" to "true",
            "Implementation-Title" to "NotificationsAPI",
            "Implementation-Version" to version
        )
    }

    archiveBaseName.set("NotificationsAPI")
}

tasks.register<Jar>("coremodJar") {
    group = "build"
    from(sourceSets.main.get().output)
    archiveClassifier.set("coremod")
    manifest.attributes(
        "FMLCorePlugin" to "com.arctyll.notificationsapi.core.NotificationsCoremod",
        "FMLCorePluginContainsMod" to "true"
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["coremodJar"])
            groupId = "com.arctyll"
            artifactId = "notificationsapi"
            version = project.version.toString()

            pom {
                name.set("NotificationsAPI")
                description.set("A Minecraft Forge 1.8.9 coremod API for displaying rich notifications.")
                url.set("https://github.com/arctyll/notificationsapi")

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
                    url.set("https://github.com/arctyll/notificationsapi")
                    connection.set("scm:git:git://github.com/arctyll/notificationsapi.git")
                    developerConnection.set("scm:git:ssh://github.com/arctyll/notificationsapi.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "ossrh"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD") ?: ""
    )
    sign(publishing.publications["mavenJava"])
}

tasks.build {
    dependsOn("coremodJar")
}
