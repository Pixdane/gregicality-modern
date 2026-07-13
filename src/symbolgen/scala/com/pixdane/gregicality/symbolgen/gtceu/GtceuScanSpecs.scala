package com.pixdane.gregicality.symbolgen.gtceu

final case class StaticFieldScanSpec(
    sourcePath: String,
    ownerFqcn: String,
    memberTypeSimpleName: String
)

final case class GtMaterialsScanSpec(
    declarationPath: String,
    assignmentDir: String,
    ownerFqcn: String,
    namespace: String,
    idFactoryFqcn: String
)
