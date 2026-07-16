package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.gtceu.{
  MaterialFlagPresetsRef,
  MaterialFlagsRef
}
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test

class MaterialFlagMetadataIntegrationTest:

  @Test
  def generatedFlagRequirementsMatchGtceuSource(): Unit =
    val requirements = MaterialFlagsRef
      .requirements(MaterialFlagsRef.GENERATE_GEAR)
      .getOrElse(fail("missing GENERATE_GEAR requirements"))

    assertEquals(
      Vector(MaterialFlagsRef.GENERATE_PLATE, MaterialFlagsRef.GENERATE_ROD),
      requirements.requiredFlags
    )
    assertEquals(
      Vector("DUST"),
      requirements.requiredProperties.map(_.path.parts.last)
    )

  @Test
  def generatedPresetMembersFlattenGtmaterialsInitialization(): Unit =
    val members = MaterialFlagPresetsRef
      .members(MaterialFlagPresetsRef.EXT2_METAL)
      .getOrElse(fail("missing EXT2_METAL members"))

    assertEquals(
      Vector(
        MaterialFlagsRef.GENERATE_PLATE,
        MaterialFlagsRef.GENERATE_ROD,
        MaterialFlagsRef.GENERATE_LONG_ROD,
        MaterialFlagsRef.GENERATE_BOLT_SCREW
      ),
      members
    )
