plugins {
    scala
}

scala {
    scalaVersion = deps.versions.scala3.get()
}

tasks.compileScala {
    dependsOn(tasks.processResources)
}
