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

val codegen = sourceSets.create("codegen") {
    scala.srcDir("src/codegen/scala")
    resources.srcDir("src/codegen/resources")

    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += output + compileClasspath
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
