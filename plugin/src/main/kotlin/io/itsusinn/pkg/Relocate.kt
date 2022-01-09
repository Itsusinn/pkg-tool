package io.itsusinn.pkg

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.itsusinn.pkg.shadowjar.kotlinRelocate
import org.gradle.api.Project

object Relocate {

  fun configure(project: Project, shadowJar: ShadowJar): Unit = with(shadowJar) {
    val ext = project.pkgExtension
    ext.relocateDependencies.forEach {
      relocate(it.first, it.second)
    }
    ext.kotlinRelocateDependencies.forEach {
      kotlinRelocate(it.first, it.second)
    }
    builtinRelocations(project).forEach {
      relocate(it.first, it.second)
    }
    if (ext.relocateKotlinStdlib){
      kotlinRelocate("kotlin.","kotlin.${project.kotlinLibVersion.replace(".","_")}.")
    }
    if (ext.relocateKotlinxLib) {
      kotlinRelocate("kotlinx.","kotlinx.relocate.")
    }
  }
  private fun builtinRelocations(project: Project): List<Pair<String, String>> = listOf(
    // "kotlin" to "kotlin.${project.kotlinLibVersion.replace(".","_")}"
  )
}
