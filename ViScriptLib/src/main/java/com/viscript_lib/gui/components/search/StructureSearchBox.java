package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 结构自动补全框，值类型为 {@code ResourceKey<Structure>}。
 */
public class StructureSearchBox extends RegistrySearchBox<ResourceKey<Structure>> {
    private static final List<ResourceKey<Structure>> BUILTIN_STRUCTURE_KEYS = List.of(
            BuiltinStructures.PILLAGER_OUTPOST,
            BuiltinStructures.MINESHAFT,
            BuiltinStructures.MINESHAFT_MESA,
            BuiltinStructures.WOODLAND_MANSION,
            BuiltinStructures.JUNGLE_TEMPLE,
            BuiltinStructures.DESERT_PYRAMID,
            BuiltinStructures.IGLOO,
            BuiltinStructures.SHIPWRECK,
            BuiltinStructures.SHIPWRECK_BEACHED,
            BuiltinStructures.SWAMP_HUT,
            BuiltinStructures.STRONGHOLD,
            BuiltinStructures.OCEAN_MONUMENT,
            BuiltinStructures.OCEAN_RUIN_COLD,
            BuiltinStructures.OCEAN_RUIN_WARM,
            BuiltinStructures.FORTRESS,
            BuiltinStructures.NETHER_FOSSIL,
            BuiltinStructures.END_CITY,
            BuiltinStructures.BURIED_TREASURE,
            BuiltinStructures.BASTION_REMNANT,
            BuiltinStructures.VILLAGE_PLAINS,
            BuiltinStructures.VILLAGE_DESERT,
            BuiltinStructures.VILLAGE_SAVANNA,
            BuiltinStructures.VILLAGE_SNOWY,
            BuiltinStructures.VILLAGE_TAIGA,
            BuiltinStructures.RUINED_PORTAL_STANDARD,
            BuiltinStructures.RUINED_PORTAL_DESERT,
            BuiltinStructures.RUINED_PORTAL_JUNGLE,
            BuiltinStructures.RUINED_PORTAL_SWAMP,
            BuiltinStructures.RUINED_PORTAL_MOUNTAIN,
            BuiltinStructures.RUINED_PORTAL_OCEAN,
            BuiltinStructures.RUINED_PORTAL_NETHER,
            BuiltinStructures.ANCIENT_CITY,
            BuiltinStructures.TRAIL_RUINS
    );

    public StructureSearchBox() {
        this(BuiltinStructures.PILLAGER_OUTPOST);
    }

    public StructureSearchBox(@Nullable ResourceKey<Structure> defaultValue) {
        super(
                defaultValue,
                StructureSearchBox::getStructureRegistry,
                StructureSearchBox::getStructureId,
                StructureSearchBox::getStructureIdString,
                StructureSearchBox::searchStructures,
                UIElementProvider.text(structure -> Component.literal(getStructureIdString(structure)))
        );
    }

    public StructureSearchBox(@Nullable Holder<Structure> defaultValue) {
        this(getStructureKey(defaultValue));
    }

    @Nullable
    public ResourceLocation getSelectedStructureId() {
        return getSelectedId();
    }

    public String getSelectedStructureIdString() {
        return getSelectedIdString();
    }

    public String getSelectedStructureTypeIdString() {
        return getStructureTypeIdString(getValue());
    }

    @Nullable
    public static ResourceKey<Structure> getStructureKey(@Nullable Holder<Structure> structure) {
        return structure == null ? null : structure.unwrapKey().orElse(null);
    }

    @Nullable
    public static Holder.Reference<Structure> getStructureHolder(ResourceKey<Structure> key) {
        var registry = getStructureRegistry();
        return registry == null ? null : registry.getHolder(key).orElse(null);
    }

    @Nullable
    public static ResourceLocation getStructureId(@Nullable ResourceKey<Structure> structure) {
        return structure == null ? null : structure.location();
    }

    public static String getStructureIdString(@Nullable ResourceKey<Structure> structure) {
        var id = getStructureId(structure);
        return id == null ? "" : id.toString();
    }

    @Nullable
    public static ResourceLocation getStructureTypeId(@Nullable ResourceKey<Structure> structure) {
        if (structure == null) {
            return null;
        }
        var holder = getStructureHolder(structure);
        return holder == null ? null : BuiltInRegistries.STRUCTURE_TYPE.getKey(holder.value().type());
    }

    public static String getStructureTypeIdString(@Nullable ResourceKey<Structure> structure) {
        var id = getStructureTypeId(structure);
        return id == null ? "" : id.toString();
    }

    @Nullable
    static Registry<Structure> getStructureRegistry() {
        var minecraft = Minecraft.getInstance();
        var connection = minecraft.getConnection();
        if (connection != null) {
            var registry = connection.registryAccess().registry(Registries.STRUCTURE).orElse(null);
            if (hasEntries(registry)) {
                return registry;
            }
        }

        if (minecraft.level != null) {
            var registry = minecraft.level.registryAccess().registry(Registries.STRUCTURE).orElse(null);
            if (hasEntries(registry)) {
                return registry;
            }
        }

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var registry = server.registryAccess().registry(Registries.STRUCTURE).orElse(null);
            if (hasEntries(registry)) {
                return registry;
            }
        }

        return null;
    }

    private static boolean hasEntries(@Nullable Registry<Structure> registry) {
        return registry != null && registry.size() > 0;
    }

    private static void searchStructures(String word, IResultHandler<ResourceKey<Structure>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        getKnownStructureKeys().stream()
                .sorted(Comparator.comparing(key -> key.location().toString()))
                .takeWhile(key -> !Thread.currentThread().isInterrupted())
                .filter(key -> matches(lowerWord, key.location().toString())
                        || matches(lowerWord, key.location().toString().replace('_', ' '))
                        || matches(lowerWord, getStructureTypeIdString(key)))
                .forEach(searchHandler::acceptResult);
    }

    private static List<ResourceKey<Structure>> getKnownStructureKeys() {
        var registry = getStructureRegistry();
        if (registry == null) {
            return BUILTIN_STRUCTURE_KEYS;
        }
        return registry.holders()
                .map(Holder.Reference::key)
                .distinct()
                .toList();
    }
}
