package io.itsusinn.pkg.shadowjar

import com.github.jengelman.gradle.plugins.shadow.relocation.RelocateClassContext
import com.github.jengelman.gradle.plugins.shadow.relocation.RelocatePathContext
import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.itsusinn.pkg.shadowjar.KotlinRelocator.Companion.storeRelocationPath
import org.gradle.api.Action
import org.objectweb.asm.* // ktlint-disable no-wildcard-imports
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class KotlinRelocator(private val task: ShadowJar, private val delegate: SimpleRelocator) : Relocator by delegate {
  override fun relocatePath(context: RelocatePathContext?): String {
    return delegate.relocatePath(context).also {
      foundRelocatedSubPaths.getOrPut(task) { hashSetOf() }.add(it.substringBeforeLast('/'))
    }
  }

  override fun relocateClass(context: RelocateClassContext?): String {
    return delegate.relocateClass(context).also {
      val packageName = it.substringBeforeLast('.')
      foundRelocatedSubPaths.getOrPut(task) { hashSetOf() }.add(packageName.replace('.', '/'))
    }
  }
  companion object {
    private val foundRelocatedSubPaths: MutableMap<ShadowJar, MutableSet<String>> = hashMapOf()
    private val relocationPaths: MutableMap<ShadowJar, MutableMap<String, String>> = hashMapOf()
    private fun getRelocationPaths(shadowJar: ShadowJar) = relocationPaths.getOrPut(shadowJar) { hashMapOf() }

    internal fun ShadowJar.storeRelocationPath(pattern: String, destination: String) {
      val newPattern = pattern.replace('.', '/') + "/"
      val taskRelocationPaths = getRelocationPaths(this)
      val intersections = taskRelocationPaths.keys.filter { it.startsWith(newPattern) }
      require(intersections.isEmpty()) {
        "Can't relocate from $pattern to $destination as it clashes with another paths: ${intersections.joinToString()}"
      }
      taskRelocationPaths[newPattern] = destination.replace('.', '/') + "/"
    }
    private fun ShadowJar.patchFile(file: Path) {
      if (Files.isDirectory(file) || !file.toString().endsWith(".class")) return
      val taskRelocationPaths = getRelocationPaths(this)
      Files.newInputStream(file).use { ins ->
        val cr = ClassReader(ins)
        val cw = PatchedClassWriter(cr, 0, taskRelocationPaths)
        val scanner = AnnotationScanner(cw, taskRelocationPaths)
        cr.accept(scanner, 0)
        if (scanner.wasPatched || cw.wasPatched) {
          ins.close()
          Files.delete(file)
          Files.write(file, cw.toByteArray())
        }
      }
    }

    fun patchMetadata(task: ShadowJar) {
      val zip = task.archiveFile.get().asFile.toPath()
      val loader: ClassLoader? = null
      FileSystems.newFileSystem(zip, loader).use { fs ->
        foundRelocatedSubPaths[task]?.forEach {
          val packagePath = fs.getPath(it)
          if (Files.exists(packagePath) && Files.isDirectory(packagePath)) {
            Files.list(packagePath).forEach { file ->
              task.patchFile(file)
            }
          }
        }
      }
    }
  }
}

fun ShadowJar.kotlinRelocate(pattern: String, destination: String, configure: Action<SimpleRelocator>) {
  val delegate = SimpleRelocator(pattern, destination, ArrayList(), ArrayList())
  configure.execute(delegate)
  storeRelocationPath(pattern, destination)
  relocate(KotlinRelocator(this, delegate))
}

fun ShadowJar.kotlinRelocate(pattern: String, destination: String) {
  kotlinRelocate(pattern, destination) {}
}
