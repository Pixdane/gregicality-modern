package com.pixdane.gregicality.common.data

import com.pixdane.gregicality.common.machine.multiblock.VoidMiner
import com.pixdane.gregicality.config.GregicalityConfig
import com.tterrag.registrate.providers.RegistrateLangProvider

/** Adds custom lang keys (tooltips, descriptions) to the generated `en_us.json`
  * via Registrate's LANG provider. Machine names are auto-collected from
  * `langValue`; this handler delegates to each machine object and config
  * section for the keys that `translatable` references but Registrate does not
  * auto-generate.
  */
object GregicalityLangHandler:

  def init(provider: RegistrateLangProvider): Unit =
    GregicalityConfig.langEntries(provider)

    VoidMiner.langEntries(provider)

end GregicalityLangHandler
