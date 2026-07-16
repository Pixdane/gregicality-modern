package com.pixdane.gregicality.codegen.core.materials

import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import com.pixdane.gregicality.core.refs.{
  FluidStorageKeyRef,
  MaterialFlagPresetRef,
  MaterialFlagRef,
  MaterialPropertyKeyRef
}

/** A validation problem found while constructing or checking material data. */
sealed trait ValidationIssue extends Product with Serializable:
  def message: String

object ValidationIssue:

  /** A scalar value that does not satisfy its lexical or numeric constraint. */
  final case class InvalidScalar(
      name: String,
      value: String,
      requirement: String
  ) extends ValidationIssue:
    override val message: String =
      s"$name value '$value' must satisfy: $requirement"

  /** Multiple declarations author the same registry path. */
  final case class DuplicateMaterialId(
      id: RegistryPath,
      fields: Vector[ScalaIdent]
  ) extends ValidationIssue:
    override val message: String =
      s"material id '${id.value}' is used by fields " +
        fields.map(_.value).mkString(", ")

  /** Multiple declarations author the same generated Scala field. */
  final case class DuplicateMaterialField(
      field: ScalaIdent,
      ids: Vector[RegistryPath]
  ) extends ValidationIssue:
    override val message: String =
      s"material field '${field.value}' is used by ids " +
        ids.map(_.value).mkString(", ")

  /** GTCEu Material.Builder rejects material paths ending in an underscore. */
  final case class MaterialIdTrailingUnderscore(id: RegistryPath)
      extends ValidationIssue:
    override val message: String =
      s"material id '${id.value}' must not end in an underscore"

  /** A new material path shadows a canonical GTCEu material path. */
  final case class CanonicalMaterialIdCollision(id: RegistryPath)
      extends ValidationIssue:
    override val message: String =
      s"new material id '${id.value}' collides with a canonical GTCEu material"

  /** The effective property view contains both mutually exclusive base forms.
    */
  final case class IngotGemConflict(field: ScalaIdent) extends ValidationIssue:
    override val message: String =
      s"material '${field.value}' has both effective ingot and gem properties"

  /** A material registers more than one fluid for the same storage key. */
  final case class DuplicateFluidKey(
      field: ScalaIdent,
      key: FluidStorageKeyRef
  ) extends ValidationIssue:
    override val message: String =
      s"material '${field.value}' repeats fluid key ${symbolName(key.path)}"

  /** An explicit primary fluid key is absent from the material's fluid entries.
    */
  final case class PrimaryFluidKeyMissing(
      field: ScalaIdent,
      primaryKey: FluidStorageKeyRef
  ) extends ValidationIssue:
    override val message: String =
      s"material '${field.value}' primary fluid key " +
        s"${symbolName(primaryKey.path)} has no matching fluid entry"

  /** Dependency metadata is unavailable for an authored material flag. */
  final case class UnknownMaterialFlag(
      field: ScalaIdent,
      flag: MaterialFlagRef
  ) extends ValidationIssue:
    override val message: String =
      s"material '${field.value}' uses unknown flag ${symbolName(flag.path)}"

  /** Member metadata is unavailable for an authored material flag preset. */
  final case class UnknownMaterialFlagPreset(
      field: ScalaIdent,
      preset: MaterialFlagPresetRef
  ) extends ValidationIssue:
    override val message: String =
      s"material '${field.value}' uses unknown flag preset " +
        symbolName(preset.path)

  /** A required flag is absent from the authored flag and preset member set. */
  final case class MissingRequiredFlag(
      field: ScalaIdent,
      flag: MaterialFlagRef,
      requiredFlag: MaterialFlagRef
  ) extends ValidationIssue:
    override val message: String =
      s"material '${field.value}' flag ${symbolName(flag.path)} requires authored " +
        s"flag ${symbolName(requiredFlag.path)}"

  /** A flag requirement is absent from the check-only effective property view.
    */
  final case class MissingRequiredProperty(
      field: ScalaIdent,
      flag: MaterialFlagRef,
      requiredProperty: MaterialPropertyKeyRef
  ) extends ValidationIssue:
    override val message: String =
      s"material '${field.value}' flag ${symbolName(flag.path)} requires " +
        s"property ${symbolName(requiredProperty.path)}"

  private def symbolName(
      path: com.pixdane.gregicality.core.refs.ScalaSymbolPath
  ): String =
    path.parts.lastOption.getOrElse(path.parts.mkString("."))

/** Validation that accumulates one or more material issues on failure. */
type ValidationResult[A] = ValidatedNec[ValidationIssue, A]

/** Namespace-free Minecraft resource path. */
opaque type RegistryPath = String

object RegistryPath:

  /** Validates Minecraft's lowercase resource-path grammar. */
  def from(value: String): ValidationResult[RegistryPath] =
    validate(
      "RegistryPath",
      value,
      value.matches("[a-z0-9/._-]+"),
      "one or more lowercase path characters [a-z0-9/._-]"
    )

  extension (path: RegistryPath)
    /** Returns the validated path string. */
    def value: String = path

/** ASCII Scala identifier used for generated declarations and symbolic names.
  */
opaque type ScalaIdent = String

object ScalaIdent:

  /** Validates identifier syntax and rejects Scala keywords. */
  def from(value: String): ValidationResult[ScalaIdent] =
    validate(
      "ScalaIdent",
      value,
      value.matches("[A-Za-z_$][A-Za-z0-9_$]*") &&
        !ScalaKeywords.contains(value),
      "an ASCII Scala identifier that is not a keyword"
    )

  extension (ident: ScalaIdent)
    /** Returns the validated identifier string. */
    def value: String = ident

/** RGB color in the inclusive range 0x000000 through 0xFFFFFF. */
opaque type HexRgb = Int

object HexRgb:

  /** Validates a 24-bit RGB integer. */
  def from(value: Int): ValidationResult[HexRgb] =
    validate("HexRgb", value, value >= 0 && value <= 0xffffff, "0..0xFFFFFF")

  extension (rgb: HexRgb)
    /** Returns the validated RGB integer. */
    def value: Int = rgb

/** Integer strictly greater than zero. */
opaque type PositiveInt = Int

object PositiveInt:

  /** Validates a positive integer. */
  def from(value: Int): ValidationResult[PositiveInt] =
    validate("PositiveInt", value, value > 0, "greater than zero")

  extension (number: PositiveInt)
    /** Returns the validated integer. */
    def value: Int = number

/** Integer greater than or equal to zero. */
opaque type NonNegativeInt = Int

object NonNegativeInt:

  /** Validates a non-negative integer. */
  def from(value: Int): ValidationResult[NonNegativeInt] =
    validate(
      "NonNegativeInt",
      value,
      value >= 0,
      "greater than or equal to zero"
    )

  extension (number: NonNegativeInt)
    /** Returns the validated integer. */
    def value: Int = number

/** Positive blast temperature in Kelvin. */
opaque type Kelvin = Int

object Kelvin:

  /** Validates a positive blast temperature. */
  def from(value: Int): ValidationResult[Kelvin] =
    validate("Kelvin", value, value > 0, "greater than zero")

  extension (temperature: Kelvin)
    /** Returns the validated Kelvin value. */
    def value: Int = temperature

/** Positive authored recipe duration in ticks. */
opaque type DurationTicks = Int

object DurationTicks:

  /** Validates a positive recipe duration. */
  def from(value: Int): ValidationResult[DurationTicks] =
    validate("DurationTicks", value, value > 0, "greater than zero")

  extension (duration: DurationTicks)
    /** Returns the validated tick count. */
    def value: Int = duration

/** Non-negative material harvest level. */
opaque type HarvestLevel = Int

object HarvestLevel:

  /** Validates a non-negative harvest level. */
  def from(value: Int): ValidationResult[HarvestLevel] =
    validate("HarvestLevel", value, value >= 0, "greater than or equal to zero")

  extension (level: HarvestLevel)
    /** Returns the validated harvest level. */
    def value: Int = level

/** Non-negative furnace burn time in ticks. */
opaque type BurnTimeTicks = Int

object BurnTimeTicks:

  /** Validates a non-negative burn time. */
  def from(value: Int): ValidationResult[BurnTimeTicks] =
    validate(
      "BurnTimeTicks",
      value,
      value >= 0,
      "greater than or equal to zero"
    )

  extension (burnTime: BurnTimeTicks)
    /** Returns the validated tick count. */
    def value: Int = burnTime

/** Positive literal voltage or EU/t value. */
opaque type Voltage = Long

object Voltage:

  /** Validates a positive voltage. */
  def from(value: Long): ValidationResult[Voltage] =
    validate("Voltage", value, value > 0, "greater than zero")

  extension (voltage: Voltage)
    /** Returns the validated voltage. */
    def value: Long = voltage

/** Non-negative explicitly authored fluid temperature in Kelvin. */
opaque type FluidTemperature = Int

object FluidTemperature:

  /** Validates a non-negative fluid temperature. */
  def from(value: Int): ValidationResult[FluidTemperature] =
    validate(
      "FluidTemperature",
      value,
      value >= 0,
      "greater than or equal to zero"
    )

  extension (temperature: FluidTemperature)
    /** Returns the validated fluid temperature. */
    def value: Int = temperature

private def validate[A](
    name: String,
    value: A,
    isValid: Boolean,
    requirement: String
): ValidationResult[A] =
  if isValid then Validated.Valid(value)
  else
    Validated.Invalid(
      NonEmptyChain.one(
        ValidationIssue.InvalidScalar(name, value.toString, requirement)
      )
    )

private val ScalaKeywords: Set[String] = Set(
  "abstract",
  "case",
  "catch",
  "class",
  "def",
  "do",
  "else",
  "enum",
  "export",
  "extends",
  "false",
  "final",
  "finally",
  "for",
  "given",
  "if",
  "implicit",
  "import",
  "inline",
  "lazy",
  "match",
  "new",
  "null",
  "object",
  "opaque",
  "open",
  "override",
  "package",
  "private",
  "protected",
  "return",
  "sealed",
  "super",
  "then",
  "this",
  "throw",
  "trait",
  "transparent",
  "true",
  "try",
  "type",
  "using",
  "val",
  "var",
  "while",
  "with",
  "yield"
)
