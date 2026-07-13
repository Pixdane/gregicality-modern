import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.process.CommandLineArgumentProvider

plugins {
    id("scala-project")
}

abstract class ScalafmtArguments : CommandLineArgumentProvider {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Input
    abstract val check: Property<Boolean>

    override fun asArguments(): Iterable<String> =
        buildList {
            add("--config")
            add(configFile.get().asFile.absolutePath)
            add("--non-interactive")

            if (check.get()) {
                add("--test")
            }

            sourceFiles.files
                .filter { it.isFile }
                .sortedBy { it.toPath().toString() }
                .forEach { add(it.absolutePath) }
        }
}

val scalafmt = configurations.create("scalafmt")

dependencies {
    add(scalafmt.name, deps.scalafmt.cli)
}

val scalafmtConfig =
    layout.projectDirectory.file(".scalafmt.conf")

val scalafmtSources =
    files(
        fileTree("src") {
            include("**/*.scala")
            exclude("generated/**")
            exclude("**/generated/**")
        }
    )

fun JavaExec.configureScalafmt(check: Boolean) {
    classpath = scalafmt
    mainClass.set("org.scalafmt.cli.Cli")
    argumentProviders.add(
        objects.newInstance(ScalafmtArguments::class.java).apply {
            configFile.set(scalafmtConfig)
            sourceFiles.from(scalafmtSources)
            this.check.set(check)
        }
    )
}

tasks.register<JavaExec>("scalafmt") {
    group = "formatting"
    description = "Formats non-generated Scala sources with Scalafmt."

    configureScalafmt(check = false)
}

tasks.register<JavaExec>("scalafmtCheck") {
    group = "verification"
    description = "Checks non-generated Scala sources with Scalafmt."

    configureScalafmt(check = true)
}
