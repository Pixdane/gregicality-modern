plugins {
    alias(conventions.plugins.scala.project)
    alias(conventions.plugins.scalafmt)
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.publish)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
}

dependencies {
    // Annotation processing
    if (useMixin) {
        annotationProcessor(variantOf(libs.mixin) { classifier("processor") })
    }

    // Scala language and libraries
    compileOnly(deps.scala3)
    compileOnly(deps.cats)

    modRuntimeOnly(variantOf(deps.scalablecatsforce) {
        classifier("with-library")
    }) {
        isTransitive = false
    }

    // Core mod dependencies
    modCompileOnly(deps.registrate)
    modImplementation(deps.gtceu)

    // ldlib is jar-in-jar'd inside GTCEu; expose it to the Scala compiler.
    compileOnly(deps.ldlib)

    // Fzzy Config and its Kotlin ABI/runtime support
    modImplementation(deps.fzzyConfig)
    compileOnly(deps.kotlinStdlib)
    compileOnly(deps.tomlkt)
    modRuntimeOnly(deps.kotlinForForge)

    // Development tools
    modRuntimeOnly(deps.bundles.jei)

    // Tests
    testImplementation(deps.kotlinStdlib)
    testImplementation(deps.tomlkt)
    testRuntimeOnly(deps.jankson)
}
