package io.itsusinn.pkg

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import shadow.org.apache.commons.io.FilenameUtils
import java.io.File
import javax.inject.Inject

@CacheableTask
open class BuildPluginTask @Inject constructor(
  @JvmField internal val target: KotlinTarget
) : ShadowJar() {

  @get:Internal
  val output: File
    get() = outputs.files.singleFile

  companion object {
    internal fun register(project: Project): BuildPluginTask = with(project) {
      tasks.findByName("shadowJar")?.enabled = false
      val target = kotlinTarget
      val buildJarTask = tasks.create(
        "pkg",
        BuildPluginTask::class.java,
        target
      )
      buildJarTask.apply jar@{
        project.configurations.create("empty")
        configurations = listOf(project.configurations.findByName("empty"))
        group = "pkg"
        archiveExtension.set("jar")
        val compilations = target.compilations.filter { it.name == KotlinCompilation.MAIN_COMPILATION_NAME }

        compilations.forEach {
          dependsOn(it.compileKotlinTask)
          from(it.output.allOutputs)
        }
        from(fileTree("${project.buildDir}/pkg-gen-files"))
        println("The following libraries in 'runtimeClasspath' and its recursive dependencies will be shadowed into the fat jar")

        from(
          project.configurations.findByName("runtimeClasspath")?.copyRecursive copy@{ dep ->
            var shouldCopy = true
            val shadow = project.configurations.findByName("shadow")
            for (excludeDep in pkgExtension.excludedDeps) {
              if (
                excludeDep.group.equals(dep.group, true) &&
                excludeDep.name.equals(dep.name, true)
              ) {
                shouldCopy = false
                break
              }
            }
            for (excludeGroup in pkgExtension.excludedGroups) {
              if (excludeGroup.equals(dep.group, true)) {
                shouldCopy = false
                break
              }
            }
            if (shouldCopy) {
              println("Group: ${dep.group}, Name: ${dep.name}, Version: ${dep.version}")
            } else {
              shadow?.dependencies?.add(dep)
            }
            shouldCopy
          }
        )

        Relocate.configure(project, this)
        exclude determine@{ file ->
          var shouldExclude = false
          val builtinExcludePaths = listOf(
            "META-INF/*.DSA", "META-INF/*.SF"
          )
          val builtinExcludePathPrefix = listOf<String>()
          for (excludePath in pkgExtension.excludedPaths + builtinExcludePaths) {
            if (FilenameUtils.wildcardMatch(file.path, excludePath)) {
              shouldExclude = true
              break
            }
          }
          if (shouldExclude) return@determine shouldExclude
          for (excludePathPrefix in pkgExtension.excludedPathPrefix + builtinExcludePathPrefix) {
            if (file.path.startsWith(excludePathPrefix)) {
              shouldExclude = true
              break
            }
          }
          shouldExclude
        }
        destinationDirectory.value(project.layout.projectDirectory.dir(project.buildDir.name).dir("pkg"))

        pkgExtension.shadowConfigurations.forEach { it.invoke(this@jar) }
      }
    }
  }
}
