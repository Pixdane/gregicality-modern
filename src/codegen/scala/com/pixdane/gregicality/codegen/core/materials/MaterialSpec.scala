package com.pixdane.gregicality.codegen.core.materials

final case class MaterialSpec(
    materialName: MaterialName,
    id: RegistryPath,
    materialForms: MaterialForms = MaterialForms(),
    materialVisual: MaterialVisual = MaterialVisual()
)

opaque type MaterialName = String
opaque type RegistryPath = String
opaque type TranslationKey = String

opaque type HexRgb = Int
