import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = "io.github.stream29"
version = "2.2"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

mavenPublishing {
    configure(
        JavaLibrary(
            JavadocJar.Javadoc(),
            true
        )
    )
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates((group as String), "streamlin", version.toString())
    pom {
        name.set("Streamlin")
        description.set("A kotlin common by Stream.")
        inceptionYear.set("2024")
        url.set("https://github.com/Stream29/Streamlin")
        licenses {
            license {
                name.set("Apache License Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("Stream29")
                name.set("Stream")
                url.set("https://github.com/Stream29/")
            }
        }
        scm {
            url.set("https://github.com/Stream29/Streamlin")
            connection.set("scm:git:git://github.com/Stream29/Streamlin.git")
            developerConnection.set("scm:git:ssh://git@github.com:Stream29/Streamlin.git")
        }
    }
}