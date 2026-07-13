import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.process.CommandLineArgumentProvider

plugins {
    id("scala-project")
}

abstract class GenerateGtRefsArguments : CommandLineArgumentProvider {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sourcesJar: ConfigurableFileCollection

    @get:Internal
    abstract val outputDir: DirectoryProperty

    override fun asArguments(): Iterable<String> =
        listOf(
            "--kind", "gtceu",
            "--sources", sourcesJar.singleFile.absolutePath,
            "--out", outputDir.get().asFile.absolutePath,
        )
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

val core = sourceSets.create("core") {
    scala.srcDir("src/core/scala")
    resources.srcDir("src/core/resources")

    runtimeClasspath += output + compileClasspath
}

val symbolgen = sourceSets.create("symbolgen") {
    scala.srcDir("src/symbolgen/scala")
    resources.srcDir("src/symbolgen/resources")

    compileClasspath += core.output
    runtimeClasspath += output + compileClasspath
}

val symbolgenTest = sourceSets.create("symbolgenTest") {
    scala.srcDir("src/symbolgenTest/scala")
    resources.srcDir("src/symbolgenTest/resources")

    compileClasspath += symbolgen.output + symbolgen.compileClasspath
    runtimeClasspath += output + compileClasspath + symbolgen.runtimeClasspath
}

val gtceuSources = configurations.create("gtceuSources") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

dependencies {
    add(core.implementationConfigurationName, deps.scala3)

    add(symbolgen.implementationConfigurationName, deps.scala3)
    add(symbolgen.implementationConfigurationName, deps.javaparser.core)
    add(symbolgen.implementationConfigurationName, deps.cats)

    add(symbolgenTest.implementationConfigurationName, platform(libs.junit.bom))
    add(symbolgenTest.implementationConfigurationName, libs.junit.jupiter)
    add(symbolgenTest.runtimeOnlyConfigurationName, libs.junit.platform.launcher)

    add(gtceuSources.name, deps.gtceu)
}

val generatedGtRefsDir =
    layout.buildDirectory.dir("generated/sources/gcyDslRefs/scala/main")

val generateGtRefs = tasks.register<JavaExec>("generateGtRefs") {
    group = "code generation"
    description = "Generates typed Scala refs from GTCEu source artifacts."

    dependsOn(tasks.named(symbolgen.classesTaskName))

    mainClass.set("com.pixdane.gregicality.symbolgen.cli.GenerateGtRefs")
    classpath = symbolgen.runtimeClasspath
    argumentProviders.add(
        objects.newInstance(GenerateGtRefsArguments::class.java).apply {
            sourcesJar.from(gtceuSources)
            outputDir.set(generatedGtRefsDir)
        }
    )

    outputs.dir(generatedGtRefsDir)
}

val codegen = sourceSets.create("codegen") {
    scala.srcDir("src/codegen/scala")
    scala.srcDir(generatedGtRefsDir)
    resources.srcDir("src/codegen/resources")

    compileClasspath += core.output
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += output + compileClasspath
}

tasks.named(codegen.classesTaskName) {
    dependsOn(generateGtRefs)
}

tasks.named("compileCodegenScala") {
    dependsOn(generateGtRefs)
}

val testSymbolgen = tasks.register<Test>("testSymbolgen") {
    group = "verification"
    description = "Runs symbolgen unit tests."

    testClassesDirs = symbolgenTest.output.classesDirs
    classpath = symbolgenTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.check {
    dependsOn(testSymbolgen)
}

val runCodegen = tasks.register<JavaExec>("runCodegen") {
    description = "Run codegen"
    dependsOn(tasks.named(codegen.classesTaskName))
    classpath = codegen.runtimeClasspath
    mainClass.set("com.pixdane.gregicality.codegen.main")
}

tasks.compileScala {
    dependsOn(runCodegen)
}
