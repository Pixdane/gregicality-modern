package com.pixdane.gregicality.common.data

import com.pixdane.gregicality.common.machine.multiblock.VoidMinerMachine
import com.tterrag.registrate.providers.RegistrateLangProvider

/** Adds custom lang keys (tooltips, descriptions) to the generated `en_us.json`
  * via Registrate's LANG provider. Machine names are auto-collected from
  * `langValue`; this handler delegates to each machine object for the keys that
  * `translatable` references but Registrate does not auto-generate.
  */
object GregicalityLangHandler:

  def init(provider: RegistrateLangProvider): Unit =
    VoidMinerMachine.langEntries(provider)
