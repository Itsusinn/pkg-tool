plugins {
  id("java-gradle-plugin")
  id("com.gradle.plugin-publish")
  java
  `maven-publish`
  kotlin("plugin.serialization")
  kotlin("jvm")
}

group = "org.meowcat"
version = "1.0.0"

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())

  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("stdlib"))

  api(
    "com.github.johnrengelman.shadow",
    "com.github.johnrengelman.shadow.gradle.plugin",
    "7.0.0"
  )
  api("org.ow2.asm", "asm", "9.2")
  api("org.ow2.asm", "asm-util", "9.2")
}
pluginBundle {
  website = "https://github.com/Itsusinn/pkg-tool"
  vcsUrl = "https://github.com/Itsusinn/pkg-tool"
}
gradlePlugin {
  plugins {
    create("pkg-tool") {
      id = "io.itsusinn.pkg"
      displayName = "Package Tool"
      description = "Package Tool"
      implementationClass = "io.itsusinn.pkg.PkgPlugin"
    }
    publishing {
      repositories {
        maven("/home/itsusinn/Workspace/maven-repo")
      }
    }
  }
}
java {
  targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.compileKotlin {
  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
  }
  sourceCompatibility = "1.8"
}
