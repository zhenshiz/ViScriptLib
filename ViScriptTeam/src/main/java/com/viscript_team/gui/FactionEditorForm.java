package com.viscript_team.gui;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigColor;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.configurator.ui.ArrayConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.ColorConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.SearchComponentConfigurator;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
final class FactionEditorForm implements IConfigurable {
    private static final String KEY = "viscript_team.faction_editor.field.";

    @Configurable(name = KEY + "name", tips = KEY + "name.tips", forceUpdate = false)
    private String name;

    @Configurable(name = KEY + "color", tips = KEY + "color.tips", forceUpdate = false)
    @ConfigColor
    private int color;

    @Configurable(name = KEY + "friendly_fire", tips = KEY + "friendly_fire.tips", forceUpdate = false)
    private boolean friendlyFire;

    @Configurable(name = KEY + "attack_enemies", tips = KEY + "attack_enemies.tips", forceUpdate = false)
    private boolean attackEnemyFactions;

    @Configurable(
            name = KEY + "enemies",
            tips = KEY + "enemies.tips",
            collapse = false,
            canCollapse = false,
            forceUpdate = false
    )
    @ConfigList(
            configuratorMethod = "createEnemyFactionConfigurator",
            addDefaultMethod = "createDefaultEnemyFaction",
            canReorder = false
    )
    private List<String> enemyFactions = new ArrayList<>();

    private final String ownFactionId;
    private final Set<String> availableFactionIds;

    FactionEditorForm(String ownFactionId, String name, int color, boolean friendlyFire,
                      boolean attackEnemyFactions, Collection<String> enemyFactions,
                      Collection<String> availableFactionIds) {
        this.ownFactionId = ownFactionId;
        this.name = name;
        this.color = 0xFF000000 | (color & 0xFFFFFF);
        this.friendlyFire = friendlyFire;
        this.attackEnemyFactions = attackEnemyFactions;
        this.enemyFactions.addAll(enemyFactions);
        this.availableFactionIds = new LinkedHashSet<>(availableFactionIds);
        this.availableFactionIds.remove(ownFactionId);
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        IConfigurable.super.buildConfigurator(father);
        for (Configurator configurator : father.getConfigurators()) {
            configurator.label.textStyle(style -> style.fontSize(7).textWrap(TextWrap.HOVER_ROLL));
            if (configurator instanceof ColorConfigurator colorConfigurator) {
                colorConfigurator.colorSelector.alphaSlider.setDisplay(false);
            } else if (configurator instanceof ArrayConfiguratorGroup<?> listConfigurator) {
                listConfigurator.setCanAdd(!availableFactionIds.isEmpty());
            }
        }
    }

    CompoundTag toSettingsTag() {
        CompoundTag settings = new CompoundTag();
        settings.putString("name", name);
        settings.putInt("color", color & 0xFFFFFF);
        settings.putBoolean("friendlyFire", friendlyFire);
        settings.putBoolean("attackEnemyFactions", attackEnemyFactions);

        ListTag enemies = new ListTag();
        enemyFactions.stream()
                .map(value -> value == null ? "" : value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank() && !value.equals(ownFactionId))
                .distinct()
                .map(StringTag::valueOf)
                .forEach(enemies::add);
        settings.put("enemyFactions", enemies);
        return settings;
    }

    private Configurator createEnemyFactionConfigurator(Supplier<String> getter, Consumer<String> setter) {
        SearchComponentConfigurator<String> configurator = new SearchComponentConfigurator<>(
                "",
                getter,
                setter,
                "",
                false,
                (word, result) -> {
                    String query = word == null ? "" : word.trim().toLowerCase(Locale.ROOT);
                    String currentValue = getter.get();
                    availableFactionIds.stream()
                            .filter(id -> id.toLowerCase(Locale.ROOT).contains(query))
                            .filter(id -> id.equals(currentValue) || !enemyFactions.contains(id))
                            .sorted(Comparator.naturalOrder())
                            .forEach(result);
                },
                value -> value == null ? "" : value,
                value -> new Label()
                        .setText(Component.literal(value == null ? "" : value))
                        .textStyle(style -> style.fontSize(6).textWrap(TextWrap.HOVER_ROLL))
                        .setOverflowVisible(false)
        );
        configurator.searchComponent.textField.textFieldStyle(style -> style.fontSize(6));
        configurator.searchComponent.searchStyle(style -> style
                .maxItemCount(8)
                .scrollerViewHeight(72));
        return configurator;
    }

    private String createDefaultEnemyFaction() {
        return availableFactionIds.stream()
                .filter(id -> !enemyFactions.contains(id))
                .sorted()
                .findFirst()
                .orElse("");
    }
}
