package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 维度 ID 自动补全框，值类型为 {@code ResourceKey<Level>}。
 */
public class DimensionSearchBox extends RegistrySearchBox<ResourceKey<Level>> {

    public DimensionSearchBox() {
        this(Level.OVERWORLD);
    }

    public DimensionSearchBox(ResourceKey<Level> defaultValue) {
        super(
                defaultValue,
                DimensionSearchBox::getLevelStemRegistry,
                DimensionSearchBox::getDimensionId,
                DimensionSearchBox::getDimensionIdString,
                DimensionSearchBox::searchDimensions,
                UIElementProvider.text(key -> Component.literal(key.location().toString()))
        );
    }

    @Nullable
    public ResourceLocation getSelectedDimensionId() {
        return getSelectedId();
    }

    public String getSelectedDimensionIdString() {
        return getSelectedIdString();
    }

    @Nullable
    public static ResourceLocation getDimensionId(@Nullable ResourceKey<Level> dimension) {
        return dimension == null ? null : dimension.location();
    }

    public static String getDimensionIdString(@Nullable ResourceKey<Level> dimension) {
        var id = getDimensionId(dimension);
        return id == null ? "" : id.toString();
    }

    private static void searchDimensions(String word, IResultHandler<ResourceKey<Level>> searchHandler) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        getKnownDimensionKeys().stream()
                .sorted(Comparator.comparing(key -> key.location().toString()))
                .takeWhile(key -> !Thread.currentThread().isInterrupted())
                .filter(key -> matches(lowerWord, key.location().toString()))
                .forEach(searchHandler::acceptResult);
    }

    private static List<ResourceKey<Level>> getKnownDimensionKeys() {
        var dimensions = new ArrayList<ResourceKey<Level>>();
        var minecraft = Minecraft.getInstance();
        var connection = minecraft.getConnection();
        if (connection != null) {
            dimensions.addAll(connection.levels());
        }

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            dimensions.addAll(server.levelKeys());
        }

        var registry = getLevelStemRegistry();
        if (registry != null) {
            registry.holders()
                    .map(holder -> Registries.levelStemToLevel(holder.key()))
                    .forEach(dimensions::add);
        }

        return dimensions.stream().distinct().toList();
    }

    @Nullable
    static Registry<LevelStem> getLevelStemRegistry() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        return minecraft.level.registryAccess().registry(Registries.LEVEL_STEM).orElse(null);
    }
}
