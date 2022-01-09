package io.itsusinn.pkg

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

open class PkgExtension {

  internal val shadowConfigurations: MutableList<ShadowJar.() -> Unit> = mutableListOf()
  internal val excludedDeps: MutableSet<ExcludedDependency> = mutableSetOf()
  internal val excludedPaths: MutableSet<String> = mutableSetOf()
  internal val excludedPathPrefix: MutableSet<String> = mutableSetOf()
  internal val excludedGroups: MutableSet<String> = mutableSetOf()
  internal val relocateDependencies: MutableList<Pair<String, String>> = mutableListOf()
  internal val kotlinRelocateDependencies: MutableList<Pair<String, String>> = mutableListOf()
  internal val excludedRelocateDependencies: MutableSet<ExcludedDependency> = mutableSetOf()

  internal var relocateKotlinStdlib: Boolean = false
  internal var relocateKotlinxLib: Boolean = false

  fun excludeDep(group: String, name: String) {
    excludedDeps.add(ExcludedDependency(group, name))
  }
  fun excludePath(path: String) {
    excludedPaths.add(path)
  }
  fun excludeGroup(group: String) {
    excludedGroups.add(group)
  }
  fun excludeGroups(vararg groups: String) {
    excludedGroups.addAll(groups)
  }
  fun excludePathStartWith(prefix: String) {
    excludedPathPrefix.add(prefix)
  }
  fun relocate(pre: String, post: String) {
    relocateDependencies.add(pre to post)
  }
  fun kotlinRelocate(pre: String, post: String) {
    kotlinRelocateDependencies.add(pre to post)
  }
  fun relocateKotlinStdlib() {
    relocateKotlinStdlib = true
  }
  fun relocateKotlinxLib() {
    relocateKotlinxLib = true
  }

  fun shadowJar(configure: ShadowJar.() -> Unit) {
    shadowConfigurations.add(configure)
  }
  internal data class ExcludedDependency(
    val group: String,
    val name: String
  )
}
