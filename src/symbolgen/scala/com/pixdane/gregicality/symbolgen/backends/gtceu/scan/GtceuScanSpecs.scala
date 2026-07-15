package com.pixdane.gregicality.symbolgen.backends.gtceu.scan

/** Configuration for scanning a single static-field source file: which `.java`
  * path to parse, the owner class its fields belong to, and the simple name of
  * the field type to collect.
  */
final case class StaticFieldScanSpec(
    sourcePath: String,
    ownerFqcn: String,
    memberTypeSimpleName: String
)

/** Configuration for the GTCEu materials scan: the declaration file, the
  * directory of builder assignment files, the owner class, the registry
  * namespace, and the id factory used to read material ids.
  */
final case class GtMaterialsScanSpec(
    declarationPath: String,
    assignmentDir: String,
    ownerFqcn: String,
    namespace: String,
    idFactoryFqcn: String
)
