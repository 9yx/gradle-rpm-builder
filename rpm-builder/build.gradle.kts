plugins {
    `maven-publish`
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.72"
}

group = "ru.ingins.gradle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val packagerVersion: String by project
val plexusUtilsVersion: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.eclipse.packager:packager-rpm:$packagerVersion")
    implementation("org.codehaus.plexus:plexus-utils:$plexusUtilsVersion")
}

gradlePlugin {
    plugins {
        create("eclipse-packager") {
            id = "ru.ingins.gradle.rpm.builder"
            implementationClass = "ru.ingins.gradle.rpm.builder.RpmBuilderPlugin"
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "13"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "13"
    }
}