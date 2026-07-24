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
    // Mixin annotation processor for compile-time @Shadow/@Inject support
    if (useMixin) {
        annotationProcessor(variantOf(libs.mixin) { classifier("processor") })
    }

    compileOnly(deps.scala3)
    compileOnly(deps.cats)

    modCompileOnly(deps.registrate)

    modImplementation(deps.gtceu)

    // ldlib is jar-in-jar'd inside gtceu; expose it as a compileOnly Maven
    // dependency so the Scala compiler can resolve ldlib types (BlockInfo,
    // IEnhancedManaged, etc.).
    compileOnly(deps.ldlib)

    modRuntimeOnly(variantOf(deps.scalablecatsforce) {
        classifier("with-library")
    }) {
        isTransitive = false
    }

    modRuntimeOnly(deps.bundles.jei)
}
