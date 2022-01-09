package io.itsusinn.pkg

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal val Project.kotlinTarget: KotlinTarget
  get() {
    val kotlinExtension = extensions.findByType(KotlinProjectExtension::class.java)
      ?: error("Kotlin gradle plugin not applied.")
    return when (kotlinExtension) {
      is KotlinSingleTargetExtension -> kotlinExtension.target
      else -> error("Kotlin Multiplatform is not supported.")
    }
  }
internal val Project.kotlinLibVersion: String
  get() {
    val kotlinExtension = extensions.findByType(KotlinProjectExtension::class.java)
      ?: error("Kotlin gradle plugin not applied.")
    return kotlinExtension.coreLibrariesVersion
  }
internal val Project.pkgExtension: PkgExtension
  get() = extensions.findByType(PkgExtension::class.java)
    ?: error("Project:${this.name}'s TaboolibGradleExtension not found")
