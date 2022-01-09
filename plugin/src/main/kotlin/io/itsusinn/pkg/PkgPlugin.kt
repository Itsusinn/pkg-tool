package io.itsusinn.pkg // ktlint-disable filename

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class PkgPlugin : Plugin<Project> {
  override fun apply(project: Project) = with(project) {
    extensions.create("pkg", PkgExtension::class.java)

    plugins.apply(JavaPlugin::class.java)
    plugins.apply(ShadowPlugin::class.java)

    afterEvaluate {
      // register task for packaging a fat/uber jar
      BuildPluginTask.register(project)
    }
  }
}
