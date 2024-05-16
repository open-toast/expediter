import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.withGroovyBuilder

abstract class InstrumentWithJacocoOfflineTask : DefaultTask() {
    @InputFiles
    lateinit var classpath: Configuration

    @InputFile
    lateinit var jar: Provider<RegularFile>

    @OutputDirectory
    lateinit var dir: Provider<Directory>

    @TaskAction
    fun instrument() {
        val file = jar.get().asFile

        project.ant.withGroovyBuilder {
            "taskdef"(
                "name" to "instrument",
                "classname" to "org.jacoco.ant.InstrumentTask",
                "classpath" to classpath.asPath
            )
            "instrument"("destdir" to dir.get().asFile.path) {
                "fileset"("dir" to file.parent, "includes" to file.name)
            }
        }
    }
}