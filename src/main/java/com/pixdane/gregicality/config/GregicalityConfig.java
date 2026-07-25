package com.pixdane.gregicality.config;

import com.pixdane.gregicality.Tags;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

/**
 * Root config holder, written to {@code config/gregicality.yaml}.
 *
 * <p>This class is deliberately <b>Java, not Scala</b>. Toma discovers config fields with
 * {@link Class#getFields()}, which only sees {@code public} fields. A Scala {@code var} always
 * compiles to a private backing field plus accessor methods, so a Scala holder registers with zero
 * fields and silently writes an empty config file. There is no Scala annotation that emits a public
 * Java field, so the holder and its categories must stay here.
 *
 * <p>Consequences of that same reflection contract, all of which the fields below obey:
 * <ul>
 *   <li>every persisted field is {@code public}, non-{@code static}, non-{@code final};
 *   <li>every persisted field carries {@link Configurable}, including the category fields;
 *   <li>categories are plain classes with a no-arg constructor, instantiated eagerly.
 * </ul>
 *
 * <p>Read values through {@link #INSTANCE} (or the {@code features()} / {@code machines()}
 * helpers). Toma owns that instance and rewrites its fields from disk; a locally constructed
 * instance only ever holds the defaults.
 */
@Config(id = Tags.MOD_ID)
public final class GregicalityConfig {

    /** Toma-owned config instance. Assigned by {@link #init()} during mod construction. */
    public static GregicalityConfig INSTANCE;

    /** The holder, kept for {@code ConfigIO} operations and file-refresh listeners. */
    public static ConfigHolder<GregicalityConfig> HOLDER;

    private static final Object LOCK = new Object();

    /**
     * Registers the config with Toma and loads it from disk. Idempotent.
     *
     * <p>Must run during mod construction, before anything reads a config value.
     */
    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                HOLDER = Configuration.registerConfig(
                        GregicalityConfig.class,
                        // Deprecated in the 3.1.0 build we compile against, which prefers the
                        // ConfigFormats.YAML constant. Keep the method call: the 2.2.0 build gtceu
                        // ships at runtime has no such constant, only this factory.
                        ConfigFormats.yaml());
                INSTANCE = HOLDER.getConfigInstance();
            }
        }
    }

    /** Shorthand for {@code INSTANCE.features}. */
    public static Features features() {
        return INSTANCE.features;
    }

    /** Shorthand for {@code INSTANCE.machines}. */
    public static Machines machines() {
        return INSTANCE.machines;
    }

    @Configurable
    @Configurable.Comment("Toggles for optional Gregicality content.")
    public Features features = new Features();

    @Configurable
    @Configurable.Comment("Balance tuning for Gregicality machines.")
    public Machines machines = new Machines();

    public static final class Features {

        @Configurable
        @Configurable.Comment({ "Whether the Void Miner multiblocks are registered.", "Default: true" })
        public boolean enableVoidMiner = true;
    }

    public static final class Machines {

        @Configurable
        @Configurable.Comment({
                "Maximum operating temperature of the Void Miner, per tier (MK I / MK II / MK III).",
                "Also caps ore output, which scales with temperature.",
                "Default: [9000, 16000, 25000]" })
        @Configurable.FixedSize
        public int[] voidMinerMaxTemperature = { 9000, 16000, 25000 };

        @Configurable
        @Configurable.Comment({
                "Base drilling fluid consumption of the Void Miner, in mB per operation.",
                "Default: 100" })
        @Configurable.Range(min = 1, max = 100000)
        public int voidMinerFluidConsumption = 100;

        @Configurable
        @Configurable.Comment({
                "Whether to add all ore dictionary variants to the Void Miner's ore table.",
                "If false, only the first ore in the material's ore dictionary is added.",
                "Default: true" })
        public boolean voidMinerOreVariants = true;

        @Configurable
        @Configurable.Comment({
                "Ore names blacklisted from the MK I Void Miner's ore table.",
                "Default: [\"trinium\", \"triniite\"]" })
        public String[] voidMinerOreBlacklist = { "trinium", "triniite" };

        @Configurable
        @Configurable.Comment({
                "Ore names blacklisted from the MK II Void Miner's ore table.",
                "Default: []" })
        public String[] voidMinerOreBlacklistMK2 = { "" };

        @Configurable
        @Configurable.Comment({
                "Ore names blacklisted from the MK III Void Miner's ore table.",
                "Default: []" })
        public String[] voidMinerOreBlacklistMK3 = { "" };

        @Configurable
        @Configurable.Comment({
                "Items to add to the MK I Void Miner's ore table.",
                "Example: \"minecraft:wool:2\".",
                "Default: []" })
        public String[] voidMinerOreWhitelist = { "" };

        @Configurable
        @Configurable.Comment({
                "Items to add to the MK II Void Miner's ore table.",
                "Default: []" })
        public String[] voidMinerOreWhitelistMK2 = { "" };

        @Configurable
        @Configurable.Comment({
                "Items to add to the MK III Void Miner's ore table.",
                "Default: []" })
        public String[] voidMinerOreWhitelistMK3 = { "" };

        @Configurable
        @Configurable.Comment({
                "Ore processing step the Void Ore Miner produces.",
                "0 = ore, 1 = crushed, 2 = crushed purified, 3 = dust.",
                "Skipping steps reduces lag at the cost of byproducts.",
                "Default: 0" })
        @Configurable.Range(min = 0, max = 3)
        public int voidMinerOreProcStep = 0;
    }
}
