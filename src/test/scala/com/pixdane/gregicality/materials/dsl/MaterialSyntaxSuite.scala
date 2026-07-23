package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import munit.FunSuite

class MaterialSyntaxSuite extends FunSuite:
  test("visual configuration keeps one compact call shape"):
    val visual = VisualSpec(
      color = rgb"6f2200",
      iconSet = MaterialIconSet.METALLIC,
      secondary = Some(rgb"ffbb33")
    )

    assertEquals(visual.color.value, 0x6f2200)
    assertEquals(visual.secondary.map(_.value), Some(0xffbb33))
    assertEquals(visual.iconSet, MaterialIconSet.METALLIC)

  test("ore configuration groups material processing operations"):
    val sulfuricAcid: Material = null
    val tungsten: Material = null
    val titanium: Material = null
    val carbon: Material = null
    val ore = OreSpec(
      multiplier = 2,
      byproductMultiplier = 3,
      emissive = true,
      washedIn = Some(WashSpec(sulfuricAcid, 250)),
      separatedInto = List(tungsten, titanium),
      byproducts = List(carbon)
    )

    assertEquals(ore.multiplier, 2)
    assertEquals(ore.byproductMultiplier, 3)
    assert(ore.emissive)
    assertEquals(ore.washedIn.map(_.amount), Some(250))
    assertEquals(ore.separatedInto.size, 2)

  test("nested sections have distinct context markers"):
    val sections: Set[String] =
      Set(
        MaterialSection.name,
        FluidSection.name,
        BlastSection.name,
        ToolSection.name,
        ArmorSection.name,
        OreSection.name
      )

    assertEquals(sections.size, 6)
