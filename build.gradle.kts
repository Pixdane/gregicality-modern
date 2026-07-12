import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.process.CommandLineArgumentProvider

abstract class GenerateGtRefsArguments : CommandLineArgumentProvider {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sourcesJar: RegularFileProperty

    @get:Internal
    abstract val outputDir: DirectoryProperty

    override fun asArguments(): Iterable<String> =
        listOf(
            "--kind", "gtceu",
            "--sources", sourcesJar.get().asFile.absolutePath,
            "--out", outputDir.get().asFile.absolutePath,
        )
}

plugins {
    scala
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.publish)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
}

dependencies {
    // Mixin annotation processor for compile-time @Shadow/@Inject support
    if (useMixin) {
        annotationProcessor(variantOf(libs.mixin) { classifier("processor") })
    }

    // JEI for dev testing
    modRuntimeOnly(deps.bundles.jei)

    compileOnly(deps.scala3)
    compileOnly(deps.cats)
    modRuntimeOnly(variantOf(deps.scalablecatsforce) {
        classifier("with-library")
    }) {
        isTransitive = false
    }

    modImplementation(deps.gtceu)
    modCompileOnly(deps.registrate)
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

val symbolgen = sourceSets.create("symbolgen") {
    scala.srcDir("src/symbolgen/scala")
    resources.srcDir("src/symbolgen/resources")

    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += output + compileClasspath
}

val symbolgenTest = sourceSets.create("symbolgenTest") {
    scala.srcDir("src/symbolgenTest/scala")
    resources.srcDir("src/symbolgenTest/resources")

    compileClasspath += symbolgen.output + symbolgen.compileClasspath
    runtimeClasspath += output + compileClasspath + symbolgen.runtimeClasspath
}

dependencies {
    add(symbolgen.implementationConfigurationName, deps.javaparser.core)

    add(symbolgenTest.implementationConfigurationName, platform(libs.junit.bom))
    add(symbolgenTest.implementationConfigurationName, libs.junit.jupiter)
    add(symbolgenTest.runtimeOnlyConfigurationName, libs.junit.platform.launcher)
}

val generatedGtRefsDir =
    layout.buildDirectory.dir("generated/sources/gcyDslRefs/scala/main")

val gtceuSourcesJar =
    layout.file(
        providers.provider {
            val expectedName = "gtceu-1.20.1-${deps.versions.gtceu.get()}-sources.jar"
            val moduleCacheDir = gradle.gradleUserHomeDir
                .resolve("caches/modules-2/files-2.1/com.gregtechceu.gtceu/gtceu-1.20.1/${deps.versions.gtceu.get()}")

            moduleCacheDir
                .walkTopDown()
                .firstOrNull { file -> file.isFile && file.name == expectedName }
                ?: throw GradleException(
                    "Missing GTCEu sources jar in Gradle cache: $expectedName. " +
                        "Resolve com.gregtechceu.gtceu:gtceu-1.20.1:${deps.versions.gtceu.get()}:sources first."
                )
        }
    )

val generateGtRefs = tasks.register<JavaExec>("generateGtRefs") {
    group = "code generation"
    description = "Generates typed Scala refs from GTCEu source artifacts."

    dependsOn(tasks.named(symbolgen.classesTaskName))

    mainClass.set("com.pixdane.gregicality.symbolgen.GenerateRef")
    classpath = symbolgen.runtimeClasspath
    argumentProviders.add(
        objects.newInstance(GenerateGtRefsArguments::class.java).apply {
            sourcesJar.set(gtceuSourcesJar)
            outputDir.set(generatedGtRefsDir)
        }
    )

    inputs.file(gtceuSourcesJar)
        .withPropertyName("gtceuSourcesJar")
        .withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(generatedGtRefsDir)
}

val codegen = sourceSets.create("codegen") {
    scala.srcDir("src/codegen/scala")
    scala.srcDir(generatedGtRefsDir)
    resources.srcDir("src/codegen/resources")

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
    dependsOn(tasks.processResources)
    dependsOn(runCodegen)
}
