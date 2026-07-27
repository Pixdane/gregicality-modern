package com.pixdane.gregicality.config.machine.multiblock

import com.pixdane.gregicality.Tags
import me.fzzyhmstrs.fzzy_config.annotations.{
  Action,
  IgnoreVisibility,
  RequiresAction
}
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier
import me.fzzyhmstrs.fzzy_config.validation.misc.{
  ValidatedBoolean,
  ValidatedChoice
}
import me.fzzyhmstrs.fzzy_config.validation.number.{
  ValidatedInt,
  ValidatedNumber
}

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

import java.util.function.BiFunction

/** Void Miner tuning section.
  *
  * Blacklist and whitelist entries are validated for ResourceLocation syntax
  * only. Existence of the corresponding GTCEu Material or Item is resolved at
  * runtime when the Void Miner builds its candidate table — after all
  * registries are populated. Invalid IDs are silently skipped by the consumer.
  */
@IgnoreVisibility
@RequiresAction(action = Action.RESTART)
final class VoidMinerConfig extends ConfigSection:

  var enabled: ValidatedBoolean = ValidatedBoolean(true)

  /** Maximum operating temperature per voltage tier. */
  var maxTemperature: VoidMinerMaxTemperatureConfig =
    VoidMinerMaxTemperatureConfig()

  var fluidConsumption: ValidatedInt =
    ValidatedInt(100, 100000, 1, ValidatedNumber.WidgetType.TEXTBOX)

  var oreVariants: ValidatedBoolean = ValidatedBoolean(true)

  /** Material ResourceLocations excluded from the auto-generated ore table. */
  var blacklist: VoidMinerBlacklistConfig = VoidMinerBlacklistConfig()

  /** Item ResourceLocations appended to the ore table after material gen. */
  var whitelist: VoidMinerWhitelistConfig = VoidMinerWhitelistConfig()

  var oreProcessingStep: ValidatedChoice[Integer] =
    ValidatedList
      .ofInt(0, 1, 2, 3)
      .toChoices(
        ValidatedChoice.WidgetType.CYCLING,
        ValidatedChoice.translate[Integer](),
        (t: Integer, u: String) =>
          Component.translatable(u + "." + t.toString + ".desc")
      )

end VoidMinerConfig

// ---- Max Temperature section -------------------------------------------------

/** Maximum operating temperature per voltage tier. Each tier caps the allowed
  * temperature and ore output scaling.
  */
@IgnoreVisibility
final class VoidMinerMaxTemperatureConfig extends ConfigSection:

  /** Maximum temperature for the Mk I (UV) Void Miner. */
  var UV: ValidatedInt =
    ValidatedInt(9000, 100000, 1, ValidatedNumber.WidgetType.TEXTBOX)

  /** Maximum temperature for the Mk II (UHV) Void Miner. */
  var UHV: ValidatedInt =
    ValidatedInt(16000, 100000, 1, ValidatedNumber.WidgetType.TEXTBOX)

  /** Maximum temperature for the Mk III (UEV) Void Miner. */
  var UEV: ValidatedInt =
    ValidatedInt(25000, 100000, 1, ValidatedNumber.WidgetType.TEXTBOX)

end VoidMinerMaxTemperatureConfig

// ---- Blacklist section --------------------------------------------------------

/** Material IDs (ResourceLocations) to exclude, grouped by voltage tier. Only
  * ResourceLocation syntax is validated here; the consumer resolves existence
  * after GTCEu material registration completes.
  */
@IgnoreVisibility
final class VoidMinerBlacklistConfig extends ConfigSection:

  /** Blacklisted material IDs for the Mk I (UV) Void Miner. */
  var UV: ValidatedList[ResourceLocation] = ValidatedIdentifier().toList(
    ResourceLocation("gtceu", "trinium"),
    ResourceLocation(Tags.MOD_ID, "triniite")
  )

  /** Blacklisted material IDs for the Mk II (UHV) Void Miner. */
  var UHV: ValidatedList[ResourceLocation] = ValidatedIdentifier().toList()

  /** Blacklisted material IDs for the Mk III (UEV) Void Miner. */
  var UEV: ValidatedList[ResourceLocation] = ValidatedIdentifier().toList()

end VoidMinerBlacklistConfig

// ---- Whitelist section --------------------------------------------------------

/** Item IDs (ResourceLocations) to append, grouped by voltage tier. Only
  * ResourceLocation syntax is validated here; the consumer resolves existence
  * after item registration completes.
  */
@IgnoreVisibility
final class VoidMinerWhitelistConfig extends ConfigSection:

  /** Whitelisted item IDs for the Mk I (UV) Void Miner. */
  var UV: ValidatedList[ResourceLocation] = ValidatedIdentifier().toList()

  /** Whitelisted item IDs for the Mk II (UHV) Void Miner. */
  var UHV: ValidatedList[ResourceLocation] = ValidatedIdentifier().toList()

  /** Whitelisted item IDs for the Mk III (UEV) Void Miner. */
  var UEV: ValidatedList[ResourceLocation] = ValidatedIdentifier().toList()

end VoidMinerWhitelistConfig

// ---- Companion / lang entries -------------------------------------------------

object VoidMinerConfig:

  import com.pixdane.gregicality.dsl.api.LangDsl.*
  import com.tterrag.registrate.providers.RegistrateLangProvider

  def langEntries(field: String)(using provider: RegistrateLangProvider): Unit =
    translations:
      s"$field" := "Void Miner"
      s"$field.desc" := "Settings for the Void Miner multiblock family"

      // -- Scalar fields -----------------------------------------------------
      s"$field.enabled" := "Enabled"
      s"$field.enabled.desc" := "Allow Void Miner machines to operate"

      // Max Temperature section
      val mt = s"$field.maxTemperature"
      mt := "Maximum Temperature"
      s"$mt.desc" := "Maximum operating temperature for each Void Miner voltage tier"

      s"$mt.UV" := "Mk I (UV)"
      s"$mt.UV.desc" := "Maximum operating temperature for the Mk I (UV) Void Miner"

      s"$mt.UHV" := "Mk II (UHV)"
      s"$mt.UHV.desc" := "Maximum operating temperature for the Mk II (UHV) Void Miner"

      s"$mt.UEV" := "Mk III (UEV)"
      s"$mt.UEV.desc" := "Maximum operating temperature for the Mk III (UEV) Void Miner"

      s"$field.fluidConsumption" := "Fluid Consumption"
      s"$field.fluidConsumption.desc" :=
        "Base drilling-fluid consumption in mB per operation"

      s"$field.oreVariants" := "Ore Variants"
      s"$field.oreVariants.desc" :=
        "Include all registered ore-dictionary variants in the ore table"

      s"$field.oreProcessingStep" := "Ore Processing Step"
      s"$field.oreProcessingStep.desc" := "Output form produced by the Void Miner"

      // Choice value keys — index-based (0=ore, 1=crushed, 2=purified, 3=dust)
      val ops = s"$field.oreProcessingStep"
      s"$ops.0" := "Ore"
      s"$ops.0.desc" := "Produce raw ore blocks/items"
      s"$ops.1" := "Crushed Ore"
      s"$ops.1.desc" := "Produce crushed ore"
      s"$ops.2" := "Purified Crushed Ore"
      s"$ops.2.desc" := "Produce purified crushed ore"
      s"$ops.3" := "Dust"
      s"$ops.3.desc" := "Produce material dust"

      // -- Blacklist section -------------------------------------------------
      val bl = s"$field.blacklist"
      bl := "Blacklist"
      s"$bl.desc" :=
        "ResourceLocations excluded from the auto-generated ore table"

      s"$bl.UV" := "Mk I (UV)"
      s"$bl.UV.desc" :=
        "Blacklisted material IDs for the Mk I (UV) Void Miner"

      s"$bl.UHV" := "Mk II (UHV)"
      s"$bl.UHV.desc" :=
        "Blacklisted material IDs for the Mk II (UHV) Void Miner"

      s"$bl.UEV" := "Mk III (UEV)"
      s"$bl.UEV.desc" :=
        "Blacklisted material IDs for the Mk III (UEV) Void Miner"

      // -- Whitelist section -------------------------------------------------
      val wl = s"$field.whitelist"
      wl := "Whitelist"
      s"$wl.desc" :=
        "ResourceLocations appended to the ore table after material generation"

      s"$wl.UV" := "Mk I (UV)"
      s"$wl.UV.desc" := "Whitelisted item IDs for the Mk I (UV) Void Miner"

      s"$wl.UHV" := "Mk II (UHV)"
      s"$wl.UHV.desc" := "Whitelisted item IDs for the Mk II (UHV) Void Miner"

      s"$wl.UEV" := "Mk III (UEV)"
      s"$wl.UEV.desc" := "Whitelisted item IDs for the Mk III (UEV) Void Miner"

  end langEntries

end VoidMinerConfig
