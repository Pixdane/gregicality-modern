plugins {
    alias(conventions.plugins.scala.project)
    alias(conventions.plugins.scalafmt)
    alias(conventions.plugins.codegen)
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

    compileOnly(deps.scala3)
    compileOnly(deps.cats)

    modCompileOnly(deps.registrate)

    modRuntimeOnly(variantOf(deps.scalablecatsforce) {
        classifier("with-library")
    }) {
        isTransitive = false
    }
    // JEI for dev testing
    modRuntimeOnly(deps.bundles.jei)

    modImplementation(deps.gtceu)
}
