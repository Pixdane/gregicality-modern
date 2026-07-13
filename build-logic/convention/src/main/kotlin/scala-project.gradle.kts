plugins {
    scala
}

tasks.compileScala {
    dependsOn(tasks.processResources)
}
