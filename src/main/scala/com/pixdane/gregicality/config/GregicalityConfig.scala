package com.pixdane.gregicality.config

import com.pixdane.gregicality.Tags
import com.pixdane.gregicality.config.machine.multiblock.VoidMinerConfig
import com.pixdane.gregicality.dsl.api.LangDsl.*
import com.tterrag.registrate.providers.RegistrateLangProvider
import me.fzzyhmstrs.fzzy_config.annotations.{
  IgnoreVisibility,
  RootConfig,
  Version
}
import me.fzzyhmstrs.fzzy_config.api.{ConfigApiJava, RegisterType}
import me.fzzyhmstrs.fzzy_config.config.Config
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent

/** Root config, written to {@code config/gregicality/config.toml}.
  *
  * [[IgnoreVisibility]] lets Fzzy discover Scala private backing fields;
  * [[RootConfig]] places this config directly on the generated landing screen.
  *
  * The config id `gregicality:config` yields the translation-key prefix
  * `gregicality.config` (namespace.path), per Fzzy Config's default key
  * composition. All settings and subsections derive their keys from it.
  */
@RootConfig
@Version(1)
@IgnoreVisibility
final class GregicalityConfig
    extends Config(ResourceLocation(Tags.MOD_ID, "config")):

  /** Void Miner tuning section. */
  var voidMiner: VoidMinerConfig = VoidMinerConfig()

end GregicalityConfig

// ---- Companion / static API ------------------------------------------------

object GregicalityConfig:

  @volatile
  private var _instance: Option[GregicalityConfig] = None
  private val initLock: Object = new Object
  private var initialized: Boolean = false

  /** Returns the registered config instance, which reflects the loaded and (on
    * a server) client-synced values.
    *
    * @throws IllegalStateException
    *   if accessed before [[init()]] has completed.
    */
  def instance: GregicalityConfig = _instance.getOrElse(
    throw new IllegalStateException(
      "GregicalityConfig accessed before init() completed"
    )
  )

  /** Registers the config with Fzzy and loads it from disk. Idempotent;
    * thread-safe.
    *
    * Must be called during mod construction, before any machine reads config.
    */
  def init(): Unit = initLock.synchronized:
    if !initialized then
      val inst: GregicalityConfig = ConfigApiJava.registerAndLoadConfig(
        () => new GregicalityConfig(),
        RegisterType.BOTH
      )
      _instance = Some(inst)
      // Fzzy Config 0.7.6 Forge does not register its login-payload gather
      // handler. Push the synced config through its public play-phase API;
      // remove this listener when upgrading to a version that fixes the issue.
      MinecraftForge.EVENT_BUS.addListener(
        (event: PlayerEvent.PlayerLoggedInEvent) =>
          event.getEntity match
            case player: ServerPlayer =>
              ConfigApiJava.network().syncConfig(inst, player.getServer)
            case _ => ()
      )
      initialized = true

  def langEntries(provider: RegistrateLangProvider): Unit =
    given RegistrateLangProvider = provider
    val field: String = "gregicality.config"
    translations:
      field := "Gregicality"
      s"$field.desc" := "Configuration for Gregicality Modern"
      VoidMinerConfig.langEntries(field + ".voidMiner")
