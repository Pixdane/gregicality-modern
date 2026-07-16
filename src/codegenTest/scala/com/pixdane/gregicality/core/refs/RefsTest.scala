package com.pixdane.gregicality.core.refs

import org.junit.jupiter.api.Assertions.{assertEquals, assertNotEquals}
import org.junit.jupiter.api.Test

class RefsTest:

  @Test
  def fromFqcnSplitsOnDotsIntoOrderedSegments(): Unit =
    val path = ScalaSymbolPath.fromFqcn("com.example.Foo.Bar")
    assertEquals(Vector("com", "example", "Foo", "Bar"), path.parts)

  @Test
  def fromFqcnHandlesSingleSegment(): Unit =
    assertEquals(Vector("Foo"), ScalaSymbolPath.fromFqcn("Foo").parts)

  @Test
  def appendKeepsOrderAndAddsAtEnd(): Unit =
    val path = ScalaSymbolPath.fromFqcn("com.example.Foo")
    val appended = path.append("Bar")
    assertEquals(Vector("com", "example", "Foo", "Bar"), appended.parts)
    // Original is unchanged (pure data).
    assertEquals(Vector("com", "example", "Foo"), path.parts)

  @Test
  def memberAppendsMemberNameAfterOwnerSegmentsInOrder(): Unit =
    val path = ScalaSymbolPath.member("com.example.Foo", "BAR")
    assertEquals(Vector("com", "example", "Foo", "BAR"), path.parts)

  @Test
  def memberIsEquivalentToFromFqcnThenAppend(): Unit =
    val viaMember = ScalaSymbolPath.member("com.example.Foo", "Bar")
    val viaAppend = ScalaSymbolPath.fromFqcn("com.example.Foo").append("Bar")
    assertEquals(viaAppend, viaMember)

  @Test
  def existingRefsWrapPathWithNoConversion(): Unit =
    val path = ScalaSymbolPath.fromFqcn("com.example.Foo.BAR")
    // Each existing ref wraps exactly the given path; distinct ids/paths
    // produce distinct refs.
    assertEquals(
      MaterialRef(ResourceId("minecraft", "foo"), path),
      MaterialRef(ResourceId("minecraft", "foo"), path)
    )
    assertNotEquals(
      MaterialRef(ResourceId("minecraft", "foo"), path),
      MaterialRef(ResourceId("minecraft", "other"), path)
    )
    assertEquals(ElementRef(path), ElementRef(path))
    assertEquals(MaterialIconRef(path), MaterialIconRef(path))
    assertEquals(MaterialFlagRef(path), MaterialFlagRef(path))
    assertEquals(FluidAttributeRef(path), FluidAttributeRef(path))

  @Test
  def newRefsWrapPathWithNoConversion(): Unit =
    val path = ScalaSymbolPath.fromFqcn("com.example.Foo.BAR")
    assertEquals(MaterialFlagPresetRef(path), MaterialFlagPresetRef(path))
    assertEquals(MaterialPropertyKeyRef(path), MaterialPropertyKeyRef(path))
    assertEquals(FluidStorageKeyRef(path), FluidStorageKeyRef(path))
    assertEquals(GasTierRef(path), GasTierRef(path))
    assertEquals(TagPrefixRef(path), TagPrefixRef(path))
    assertEquals(HazardTriggerRef(path), HazardTriggerRef(path))
    assertEquals(MedicalConditionRef(path), MedicalConditionRef(path))
    assertEquals(ToolTypeRef(path), ToolTypeRef(path))
    assertEquals(ItemTagRef(path), ItemTagRef(path))

  @Test
  def newRefsDifferForDistinctPaths(): Unit =
    val a = ScalaSymbolPath.fromFqcn("com.example.A")
    val b = ScalaSymbolPath.fromFqcn("com.example.B")
    assertNotEquals(MaterialFlagPresetRef(a), MaterialFlagPresetRef(b))
    assertNotEquals(MaterialPropertyKeyRef(a), MaterialPropertyKeyRef(b))
    assertNotEquals(FluidStorageKeyRef(a), FluidStorageKeyRef(b))
    assertNotEquals(GasTierRef(a), GasTierRef(b))
    assertNotEquals(TagPrefixRef(a), TagPrefixRef(b))
    assertNotEquals(HazardTriggerRef(a), HazardTriggerRef(b))
    assertNotEquals(MedicalConditionRef(a), MedicalConditionRef(b))
    assertNotEquals(ToolTypeRef(a), ToolTypeRef(b))
    assertNotEquals(ItemTagRef(a), ItemTagRef(b))

  @Test
  def newRefsAreDistinctTypesEvenForSamePath(): Unit =
    // Pure-data refs are path-only and must not be cross-comparable; each
    // type is a distinct wrapper, so equality holds only within one type.
    val path = ScalaSymbolPath.fromFqcn("com.example.Foo.BAR")
    assertNotEquals(MaterialFlagPresetRef(path), FluidStorageKeyRef(path))
    assertNotEquals(GasTierRef(path), TagPrefixRef(path))
    assertNotEquals(HazardTriggerRef(path), MedicalConditionRef(path))
    assertNotEquals(ToolTypeRef(path), ItemTagRef(path))
    assertNotEquals(ElementRef(path), MaterialFlagPresetRef(path))
    assertNotEquals(MaterialFlagRef(path), MaterialFlagPresetRef(path))
    assertNotEquals(MaterialFlagRef(path), MaterialPropertyKeyRef(path))
    assertNotEquals(FluidAttributeRef(path), FluidStorageKeyRef(path))
    // Sanity: same-type, same-path still equals.
    assertEquals(FluidStorageKeyRef(path), FluidStorageKeyRef(path))

  @Test
  def materialFlagRequirementsRemainPureMetadata(): Unit =
    val path = ScalaSymbolPath.fromFqcn("com.example.Flags.GENERATE_GEAR")
    val dustPath = ScalaSymbolPath.fromFqcn("com.example.PropertyKey.DUST")
    val plate = MaterialFlagRef(path.append("GENERATE_PLATE"))
    val rod = MaterialFlagRef(path.append("GENERATE_ROD"))
    val dust = MaterialPropertyKeyRef(dustPath)
    val requirements = MaterialFlagRequirements(
      requiredFlags = Vector(plate, rod),
      requiredProperties = Vector(dust)
    )

    assertEquals(Vector(plate, rod), requirements.requiredFlags)
    assertEquals(Vector(dust), requirements.requiredProperties)
