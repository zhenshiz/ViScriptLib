package com.viscript_lib.gui.components.dev;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.test.ui.IScreenTest;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.viscript_lib.gui.components.search.AttributeSearchBox;
import com.viscript_lib.gui.components.search.BiomeSearchBox;
import com.viscript_lib.gui.components.search.BiomeTagSearchBox;
import com.viscript_lib.gui.components.search.BlockSearchBox;
import com.viscript_lib.gui.components.search.BlockTagSearchBox;
import com.viscript_lib.gui.components.search.DataPackFileSearchBox;
import com.viscript_lib.gui.components.search.DamageTypeSearchBox;
import com.viscript_lib.gui.components.search.DamageTypeTagSearchBox;
import com.viscript_lib.gui.components.search.DimensionSearchBox;
import com.viscript_lib.gui.components.search.EnchantmentSearchBox;
import com.viscript_lib.gui.components.search.EnchantmentTagSearchBox;
import com.viscript_lib.gui.components.search.EntityTypeSearchBox;
import com.viscript_lib.gui.components.search.EntityTypeTagSearchBox;
import com.viscript_lib.gui.components.search.FluidSearchBox;
import com.viscript_lib.gui.components.search.FluidTagSearchBox;
import com.viscript_lib.gui.components.search.ItemSearchBox;
import com.viscript_lib.gui.components.search.ItemTagSearchBox;
import com.viscript_lib.gui.components.search.MobEffectSearchBox;
import com.viscript_lib.gui.components.search.ParticleTypeSearchBox;
import com.viscript_lib.gui.components.search.ResourcePackFileSearchBox;
import com.viscript_lib.gui.components.search.SoundEventSearchBox;
import com.viscript_lib.gui.components.search.StructureSearchBox;
import com.viscript_lib.gui.components.search.StructureTagSearchBox;
import lombok.NoArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

@LDLRegisterClient(
        name = "viscript_search_box",
        registry = "ldlib2:screen_test",
        environment = RegistrationEnvironment.DEV_ONLY
)
@NoArgsConstructor
public class SearchBoxScreenTest implements IScreenTest {

    @Override
    public ModularUI createUI(Player entityPlayer) {
        //物品
        var itemSearchBox = new ItemSearchBox(Items.STONE);
        setupSearchBox(itemSearchBox);

        var selectedIdLabel = createValueLabel();
        var displayNameLabel = createValueLabel();
        updateItemLabels(selectedIdLabel, displayNameLabel, itemSearchBox.getValue());
        itemSearchBox.setOnValueChanged(item -> updateItemLabels(selectedIdLabel, displayNameLabel, item));

        //物品标签
        var itemTagSearchBox = new ItemTagSearchBox();
        setupSearchBox(itemTagSearchBox);

        var selectedTagIdLabel = createValueLabel();
        var selectedTagReferenceLabel = createValueLabel();
        updateItemTagLabels(selectedTagIdLabel, selectedTagReferenceLabel, itemTagSearchBox.getValue());
        itemTagSearchBox.setOnValueChanged(tag -> updateItemTagLabels(selectedTagIdLabel, selectedTagReferenceLabel, tag));

        //方块
        var blockSearchBox = new BlockSearchBox(Blocks.STONE);
        setupSearchBox(blockSearchBox);

        var selectedBlockIdLabel = createValueLabel();
        var blockDisplayNameLabel = createValueLabel();
        updateBlockLabels(selectedBlockIdLabel, blockDisplayNameLabel, blockSearchBox.getValue());
        blockSearchBox.setOnValueChanged(block -> updateBlockLabels(selectedBlockIdLabel, blockDisplayNameLabel, block));

        //方块标签
        var blockTagSearchBox = new BlockTagSearchBox();
        setupSearchBox(blockTagSearchBox);

        var selectedBlockTagIdLabel = createValueLabel();
        var selectedBlockTagReferenceLabel = createValueLabel();
        updateBlockTagLabels(selectedBlockTagIdLabel, selectedBlockTagReferenceLabel, blockTagSearchBox.getValue());
        blockTagSearchBox.setOnValueChanged(tag -> updateBlockTagLabels(selectedBlockTagIdLabel, selectedBlockTagReferenceLabel, tag));

        //流体
        var fluidSearchBox = new FluidSearchBox();
        setupSearchBox(fluidSearchBox);

        var selectedFluidIdLabel = createValueLabel();
        var fluidDisplayNameLabel = createValueLabel();
        updateFluidLabels(selectedFluidIdLabel, fluidDisplayNameLabel, fluidSearchBox.getValue());
        fluidSearchBox.setOnValueChanged(fluid -> updateFluidLabels(selectedFluidIdLabel, fluidDisplayNameLabel, fluid));

        //流体标签
        var fluidTagSearchBox = new FluidTagSearchBox();
        setupSearchBox(fluidTagSearchBox);

        var selectedFluidTagIdLabel = createValueLabel();
        var selectedFluidTagReferenceLabel = createValueLabel();
        updateFluidTagLabels(selectedFluidTagIdLabel, selectedFluidTagReferenceLabel, fluidTagSearchBox.getValue());
        fluidTagSearchBox.setOnValueChanged(tag -> updateFluidTagLabels(selectedFluidTagIdLabel, selectedFluidTagReferenceLabel, tag));

        //生物群系
        var biomeSearchBox = new BiomeSearchBox();
        setupSearchBox(biomeSearchBox);

        var selectedBiomeIdLabel = createValueLabel();
        var biomeDisplayNameLabel = createValueLabel();
        updateBiomeLabels(selectedBiomeIdLabel, biomeDisplayNameLabel, biomeSearchBox.getValue());
        biomeSearchBox.setOnValueChanged(biome -> updateBiomeLabels(selectedBiomeIdLabel, biomeDisplayNameLabel, biome));

        //生物群系标签
        var biomeTagSearchBox = new BiomeTagSearchBox();
        setupSearchBox(biomeTagSearchBox);

        var selectedBiomeTagIdLabel = createValueLabel();
        var selectedBiomeTagReferenceLabel = createValueLabel();
        updateBiomeTagLabels(selectedBiomeTagIdLabel, selectedBiomeTagReferenceLabel, biomeTagSearchBox.getValue());
        biomeTagSearchBox.setOnValueChanged(tag -> updateBiomeTagLabels(selectedBiomeTagIdLabel, selectedBiomeTagReferenceLabel, tag));

        //结构
        var structureSearchBox = new StructureSearchBox();
        setupSearchBox(structureSearchBox);

        var selectedStructureIdLabel = createValueLabel();
        var structureTypeIdLabel = createValueLabel();
        updateStructureLabels(selectedStructureIdLabel, structureTypeIdLabel, structureSearchBox.getValue());
        structureSearchBox.setOnValueChanged(structure -> updateStructureLabels(selectedStructureIdLabel, structureTypeIdLabel, structure));

        //结构标签
        var structureTagSearchBox = new StructureTagSearchBox();
        setupSearchBox(structureTagSearchBox);

        var selectedStructureTagIdLabel = createValueLabel();
        var selectedStructureTagReferenceLabel = createValueLabel();
        updateStructureTagLabels(selectedStructureTagIdLabel, selectedStructureTagReferenceLabel, structureTagSearchBox.getValue());
        structureTagSearchBox.setOnValueChanged(tag -> updateStructureTagLabels(selectedStructureTagIdLabel, selectedStructureTagReferenceLabel, tag));

        //伤害类型
        var damageTypeSearchBox = new DamageTypeSearchBox();
        setupSearchBox(damageTypeSearchBox);

        var selectedDamageTypeIdLabel = createValueLabel();
        var damageTypeMessageIdLabel = createValueLabel();
        updateDamageTypeLabels(selectedDamageTypeIdLabel, damageTypeMessageIdLabel, damageTypeSearchBox.getValue());
        damageTypeSearchBox.setOnValueChanged(damageType -> updateDamageTypeLabels(selectedDamageTypeIdLabel, damageTypeMessageIdLabel, damageType));

        //伤害类型标签
        var damageTypeTagSearchBox = new DamageTypeTagSearchBox();
        setupSearchBox(damageTypeTagSearchBox);

        var selectedDamageTypeTagIdLabel = createValueLabel();
        var selectedDamageTypeTagReferenceLabel = createValueLabel();
        updateDamageTypeTagLabels(selectedDamageTypeTagIdLabel, selectedDamageTypeTagReferenceLabel, damageTypeTagSearchBox.getValue());
        damageTypeTagSearchBox.setOnValueChanged(tag -> updateDamageTypeTagLabels(selectedDamageTypeTagIdLabel, selectedDamageTypeTagReferenceLabel, tag));

        //实体
        var entityTypeSearchBox = new EntityTypeSearchBox(EntityType.PIG);
        setupSearchBox(entityTypeSearchBox);

        var selectedEntityTypeIdLabel = createValueLabel();
        var entityTypeDisplayNameLabel = createValueLabel();
        updateEntityTypeLabels(selectedEntityTypeIdLabel, entityTypeDisplayNameLabel, entityTypeSearchBox.getValue());
        entityTypeSearchBox.setOnValueChanged(entityType -> updateEntityTypeLabels(selectedEntityTypeIdLabel, entityTypeDisplayNameLabel, entityType));

        //实体，限制为LivingEntity
        var livingEntityTypeSearchBox = new EntityTypeSearchBox(EntityType.ZOMBIE).onlyLivingEntities(entityPlayer.level());
        setupSearchBox(livingEntityTypeSearchBox);

        var selectedLivingEntityTypeIdLabel = createValueLabel();
        var livingEntityTypeDisplayNameLabel = createValueLabel();
        updateEntityTypeLabels(selectedLivingEntityTypeIdLabel, livingEntityTypeDisplayNameLabel, livingEntityTypeSearchBox.getValue());
        livingEntityTypeSearchBox.setOnValueChanged(entityType -> updateEntityTypeLabels(selectedLivingEntityTypeIdLabel, livingEntityTypeDisplayNameLabel, entityType));

        //实体，限制为Mob
        var mobEntityTypeSearchBox = new EntityTypeSearchBox(EntityType.ZOMBIE).onlyMobs(entityPlayer.level());
        setupSearchBox(mobEntityTypeSearchBox);

        var selectedMobEntityTypeIdLabel = createValueLabel();
        var mobEntityTypeDisplayNameLabel = createValueLabel();
        updateEntityTypeLabels(selectedMobEntityTypeIdLabel, mobEntityTypeDisplayNameLabel, mobEntityTypeSearchBox.getValue());
        mobEntityTypeSearchBox.setOnValueChanged(entityType -> updateEntityTypeLabels(selectedMobEntityTypeIdLabel, mobEntityTypeDisplayNameLabel, entityType));

        //实体标签
        var entityTypeTagSearchBox = new EntityTypeTagSearchBox();
        setupSearchBox(entityTypeTagSearchBox);

        var selectedEntityTypeTagIdLabel = createValueLabel();
        var selectedEntityTypeTagReferenceLabel = createValueLabel();
        updateEntityTypeTagLabels(selectedEntityTypeTagIdLabel, selectedEntityTypeTagReferenceLabel, entityTypeTagSearchBox.getValue());
        entityTypeTagSearchBox.setOnValueChanged(tag -> updateEntityTypeTagLabels(selectedEntityTypeTagIdLabel, selectedEntityTypeTagReferenceLabel, tag));

        //实体属性
        var attributeSearchBox = new AttributeSearchBox();
        setupSearchBox(attributeSearchBox);

        var selectedAttributeIdLabel = createValueLabel();
        var attributeDisplayNameLabel = createValueLabel();
        updateAttributeLabels(selectedAttributeIdLabel, attributeDisplayNameLabel, attributeSearchBox.getValue());
        attributeSearchBox.setOnValueChanged(attribute -> updateAttributeLabels(selectedAttributeIdLabel, attributeDisplayNameLabel, attribute));

        //声音事件
        var soundEventSearchBox = new SoundEventSearchBox();
        setupSearchBox(soundEventSearchBox);

        var selectedSoundEventIdLabel = createValueLabel();
        var soundEventLocationLabel = createValueLabel();
        updateSoundEventLabels(selectedSoundEventIdLabel, soundEventLocationLabel, soundEventSearchBox.getValue());
        soundEventSearchBox.setOnValueChanged(soundEvent -> updateSoundEventLabels(selectedSoundEventIdLabel, soundEventLocationLabel, soundEvent));

        //粒子类型
        var particleTypeSearchBox = new ParticleTypeSearchBox();
        setupSearchBox(particleTypeSearchBox);

        var selectedParticleTypeIdLabel = createValueLabel();
        var particleTypeOverrideLimiterLabel = createValueLabel();
        updateParticleTypeLabels(selectedParticleTypeIdLabel, particleTypeOverrideLimiterLabel, particleTypeSearchBox.getValue());
        particleTypeSearchBox.setOnValueChanged(particleType -> updateParticleTypeLabels(selectedParticleTypeIdLabel, particleTypeOverrideLimiterLabel, particleType));

        //附魔
        var enchantmentSearchBox = new EnchantmentSearchBox();
        setupSearchBox(enchantmentSearchBox);

        var selectedEnchantmentIdLabel = createValueLabel();
        var enchantmentDisplayNameLabel = createValueLabel();
        updateEnchantmentLabels(selectedEnchantmentIdLabel, enchantmentDisplayNameLabel, enchantmentSearchBox.getValue());
        enchantmentSearchBox.setOnValueChanged(enchantment -> updateEnchantmentLabels(selectedEnchantmentIdLabel, enchantmentDisplayNameLabel, enchantment));

        //附魔标签
        var enchantmentTagSearchBox = new EnchantmentTagSearchBox();
        setupSearchBox(enchantmentTagSearchBox);

        var selectedEnchantmentTagIdLabel = createValueLabel();
        var selectedEnchantmentTagReferenceLabel = createValueLabel();
        updateEnchantmentTagLabels(selectedEnchantmentTagIdLabel, selectedEnchantmentTagReferenceLabel, enchantmentTagSearchBox.getValue());
        enchantmentTagSearchBox.setOnValueChanged(tag -> updateEnchantmentTagLabels(selectedEnchantmentTagIdLabel, selectedEnchantmentTagReferenceLabel, tag));

        //状态效果
        var mobEffectSearchBox = new MobEffectSearchBox();
        setupSearchBox(mobEffectSearchBox);

        var selectedMobEffectIdLabel = createValueLabel();
        var mobEffectDisplayNameLabel = createValueLabel();
        updateMobEffectLabels(selectedMobEffectIdLabel, mobEffectDisplayNameLabel, mobEffectSearchBox.getValue());
        mobEffectSearchBox.setOnValueChanged(mobEffect -> updateMobEffectLabels(selectedMobEffectIdLabel, mobEffectDisplayNameLabel, mobEffect));

        //维度
        var dimensionSearchBox = new DimensionSearchBox();
        setupSearchBox(dimensionSearchBox);

        var selectedDimensionIdLabel = createValueLabel();
        var selectedDimensionKeyLabel = createValueLabel();
        updateDimensionLabels(selectedDimensionIdLabel, selectedDimensionKeyLabel, dimensionSearchBox.getValue());
        dimensionSearchBox.setOnValueChanged(dimension -> updateDimensionLabels(selectedDimensionIdLabel, selectedDimensionKeyLabel, dimension));

        //数据包文件
        var dataPackFileSearchBox = new DataPackFileSearchBox("advancement");
        setupSearchBox(dataPackFileSearchBox);

        var selectedDataPackFileIdLabel = createValueLabel();
        var dataPackFilePathPrefixLabel = createValueLabel();
        updateDataPackFileLabels(selectedDataPackFileIdLabel, dataPackFilePathPrefixLabel, dataPackFileSearchBox);
        dataPackFileSearchBox.setOnValueChanged(file -> updateDataPackFileLabels(selectedDataPackFileIdLabel, dataPackFilePathPrefixLabel, dataPackFileSearchBox));

        //后处理器shader JSON文件
        var postShaderSearchBox = new ResourcePackFileSearchBox("shaders/post");
        setupSearchBox(postShaderSearchBox);

        var selectedPostShaderIdLabel = createValueLabel();
        var postShaderPathPrefixLabel = createValueLabel();
        updatePostShaderLabels(selectedPostShaderIdLabel, postShaderPathPrefixLabel, postShaderSearchBox);
        postShaderSearchBox.setOnValueChanged(file -> updatePostShaderLabels(selectedPostShaderIdLabel, postShaderPathPrefixLabel, postShaderSearchBox));

        var content = new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.paddingAll(4);
            layout.gapRow(8);
        }).addChildren(
                createSection("ITEM", itemSearchBox, selectedIdLabel, displayNameLabel),
                createSection("ITEM_TAG", itemTagSearchBox, selectedTagIdLabel, selectedTagReferenceLabel),
                createSection("BLOCK", blockSearchBox, selectedBlockIdLabel, blockDisplayNameLabel),
                createSection("BLOCK_TAG", blockTagSearchBox, selectedBlockTagIdLabel, selectedBlockTagReferenceLabel),
                createSection("FLUID", fluidSearchBox, selectedFluidIdLabel, fluidDisplayNameLabel),
                createSection("FLUID_TAG", fluidTagSearchBox, selectedFluidTagIdLabel, selectedFluidTagReferenceLabel),
                createSection("BIOME", biomeSearchBox, selectedBiomeIdLabel, biomeDisplayNameLabel),
                createSection("BIOME_TAG", biomeTagSearchBox, selectedBiomeTagIdLabel, selectedBiomeTagReferenceLabel),
                createSection("STRUCTURE", structureSearchBox, selectedStructureIdLabel, structureTypeIdLabel),
                createSection("STRUCTURE_TAG", structureTagSearchBox, selectedStructureTagIdLabel, selectedStructureTagReferenceLabel),
                createSection("DAMAGE_TYPE", damageTypeSearchBox, selectedDamageTypeIdLabel, damageTypeMessageIdLabel),
                createSection("DAMAGE_TYPE_TAG", damageTypeTagSearchBox, selectedDamageTypeTagIdLabel, selectedDamageTypeTagReferenceLabel),
                createSection("ENTITY_TYPE", entityTypeSearchBox, selectedEntityTypeIdLabel, entityTypeDisplayNameLabel),
                createSection("ENTITY_TYPE_LIVING", livingEntityTypeSearchBox, selectedLivingEntityTypeIdLabel, livingEntityTypeDisplayNameLabel),
                createSection("ENTITY_TYPE_MOB", mobEntityTypeSearchBox, selectedMobEntityTypeIdLabel, mobEntityTypeDisplayNameLabel),
                createSection("ENTITY_TYPE_TAG", entityTypeTagSearchBox, selectedEntityTypeTagIdLabel, selectedEntityTypeTagReferenceLabel),
                createSection("ATTRIBUTE", attributeSearchBox, selectedAttributeIdLabel, attributeDisplayNameLabel),
                createSection("SOUND_EVENT", soundEventSearchBox, selectedSoundEventIdLabel, soundEventLocationLabel),
                createSection("PARTICLE_TYPE", particleTypeSearchBox, selectedParticleTypeIdLabel, particleTypeOverrideLimiterLabel),
                createSection("ENCHANTMENT", enchantmentSearchBox, selectedEnchantmentIdLabel, enchantmentDisplayNameLabel),
                createSection("ENCHANTMENT_TAG", enchantmentTagSearchBox, selectedEnchantmentTagIdLabel, selectedEnchantmentTagReferenceLabel),
                createSection("MOB_EFFECT", mobEffectSearchBox, selectedMobEffectIdLabel, mobEffectDisplayNameLabel),
                createSection("DIMENSION", dimensionSearchBox, selectedDimensionIdLabel, selectedDimensionKeyLabel),
                createSection("DATA_PACK_FILE", dataPackFileSearchBox, selectedDataPackFileIdLabel, dataPackFilePathPrefixLabel),
                createSection("POST_SHADER_JSON", postShaderSearchBox, selectedPostShaderIdLabel, postShaderPathPrefixLabel)
        );

        var scrollerView = new ScrollerView()
                .scrollerStyle(style -> style
                        .mode(ScrollerMode.VERTICAL)
                        .verticalScrollDisplay(ScrollDisplay.ALWAYS));
        scrollerView.layout(layout -> {
            layout.widthPercent(100);
            layout.height(190);
        });
        scrollerView.addScrollViewChild(content);

        var root = new UIElement().layout(layout -> {
            layout.width(390);
            layout.height(235);
            layout.paddingAll(8);
            layout.gapRow(6);
        }).style(style -> style.backgroundTexture(Sprites.BORDER));

        root.addChildren(
                new Label()
                        .setText(Component.literal("Search Boxes"))
                        .textStyle(style -> style
                                .fontSize(14)
                                .textAlignHorizontal(Horizontal.CENTER)
                                .textAlignVertical(Vertical.CENTER))
                        .layout(layout -> layout.height(18)),
                scrollerView
        );

        return new ModularUI(UI.of(root), entityPlayer)
                .shouldCloseOnEsc(true)
                .shouldCloseOnKeyInventory(false);
    }

    private static void setupSearchBox(SearchComponent<?> searchBox) {
        searchBox.layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
        searchBox.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
    }

    private static UIElement createSection(String title, UIElement searchBox, Label firstLine, Label secondLine) {
        return new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.paddingAll(6);
            layout.gapRow(4);
        }).style(style -> style.backgroundTexture(Sprites.RECT_DARK)).addChildren(
                new Label()
                        .setText(Component.literal(title))
                        .textStyle(style -> style
                                .fontSize(12)
                                .textAlignVertical(Vertical.CENTER))
                        .layout(layout -> layout.height(16)),
                searchBox,
                firstLine,
                secondLine
        );
    }

    private static Label createValueLabel() {
        var label = new Label();
        label.textStyle(style -> style
                .fontSize(10)
                .textAlignVertical(Vertical.CENTER));
        label.layout(layout -> layout.height(14));
        return label;
    }

    private static void updateItemLabels(Label selectedIdLabel, Label displayNameLabel, Item item) {
        selectedIdLabel.setText(Component.literal("Item ID: " + ItemSearchBox.getItemIdString(item)));
        displayNameLabel.setText(Component.literal("Display Name: " + (item == null ? "" : LocalizationUtils.format(item.getDescriptionId()))));
    }

    private static void updateItemTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<Item> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + ItemTagSearchBox.getItemTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + ItemTagSearchBox.getItemTagReferenceString(tag)));
    }

    private static void updateBlockLabels(Label selectedIdLabel, Label displayNameLabel, Block block) {
        selectedIdLabel.setText(Component.literal("Block ID: " + BlockSearchBox.getBlockIdString(block)));
        displayNameLabel.setText(Component.literal("Display Name: " + (block == null ? "" : LocalizationUtils.format(block.getDescriptionId()))));
    }

    private static void updateBlockTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<Block> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + BlockTagSearchBox.getBlockTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + BlockTagSearchBox.getBlockTagReferenceString(tag)));
    }

    private static void updateFluidLabels(Label selectedIdLabel, Label displayNameLabel, Fluid fluid) {
        selectedIdLabel.setText(Component.literal("Fluid ID: " + FluidSearchBox.getFluidIdString(fluid)));
        displayNameLabel.setText(Component.literal("Display Name: " + (fluid == null ? "" : fluid.getFluidType().getDescription().getString())));
    }

    private static void updateFluidTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<Fluid> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + FluidTagSearchBox.getFluidTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + FluidTagSearchBox.getFluidTagReferenceString(tag)));
    }

    private static void updateBiomeLabels(Label selectedIdLabel, Label displayNameLabel, Holder<Biome> biome) {
        selectedIdLabel.setText(Component.literal("Biome ID: " + BiomeSearchBox.getBiomeIdString(biome)));
        displayNameLabel.setText(Component.literal("Display Name: " + (biome == null ? "" : BiomeSearchBox.getBiomeDisplayName(biome).getString())));
    }

    private static void updateBiomeTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<Biome> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + BiomeTagSearchBox.getBiomeTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + BiomeTagSearchBox.getBiomeTagReferenceString(tag)));
    }

    private static void updateStructureLabels(Label selectedIdLabel, Label structureTypeIdLabel, ResourceKey<Structure> structure) {
        selectedIdLabel.setText(Component.literal("Structure ID: " + StructureSearchBox.getStructureIdString(structure)));
        structureTypeIdLabel.setText(Component.literal("Structure Type: " + StructureSearchBox.getStructureTypeIdString(structure)));
    }

    private static void updateStructureTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<Structure> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + StructureTagSearchBox.getStructureTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + StructureTagSearchBox.getStructureTagReferenceString(tag)));
    }

    private static void updateDamageTypeLabels(Label selectedIdLabel, Label messageIdLabel, Holder<DamageType> damageType) {
        selectedIdLabel.setText(Component.literal("Damage Type ID: " + DamageTypeSearchBox.getDamageTypeIdString(damageType)));
        messageIdLabel.setText(Component.literal("Message ID: " + DamageTypeSearchBox.getDamageTypeMessageId(damageType)));
    }

    private static void updateDamageTypeTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<DamageType> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + DamageTypeTagSearchBox.getDamageTypeTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + DamageTypeTagSearchBox.getDamageTypeTagReferenceString(tag)));
    }

    private static void updateEntityTypeLabels(Label selectedIdLabel, Label displayNameLabel, EntityType<?> entityType) {
        selectedIdLabel.setText(Component.literal("Entity Type ID: " + EntityTypeSearchBox.getEntityTypeIdString(entityType)));
        displayNameLabel.setText(Component.literal("Display Name: " + (entityType == null ? "" : LocalizationUtils.format(entityType.getDescriptionId()))));
    }

    private static void updateEntityTypeTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<EntityType<?>> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + EntityTypeTagSearchBox.getEntityTypeTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + EntityTypeTagSearchBox.getEntityTypeTagReferenceString(tag)));
    }

    private static void updateAttributeLabels(Label selectedIdLabel, Label displayNameLabel, Holder<Attribute> attribute) {
        selectedIdLabel.setText(Component.literal("Attribute ID: " + AttributeSearchBox.getAttributeIdString(attribute)));
        displayNameLabel.setText(Component.literal("Display Name: " + (attribute == null ? "" : LocalizationUtils.format(attribute.value().getDescriptionId()))));
    }

    private static void updateSoundEventLabels(Label selectedIdLabel, Label soundLocationLabel, SoundEvent soundEvent) {
        selectedIdLabel.setText(Component.literal("Sound Event ID: " + SoundEventSearchBox.getSoundEventIdString(soundEvent)));
        soundLocationLabel.setText(Component.literal("Sound ID: " + SoundEventSearchBox.getSoundLocationString(soundEvent)));
    }

    private static void updateParticleTypeLabels(Label selectedIdLabel, Label overrideLimiterLabel, ParticleType<?> particleType) {
        selectedIdLabel.setText(Component.literal("Particle Type ID: " + ParticleTypeSearchBox.getParticleTypeIdString(particleType)));
        overrideLimiterLabel.setText(Component.literal("Override Limiter: " + ParticleTypeSearchBox.particleTypeOverridesLimiter(particleType)));
    }

    private static void updateEnchantmentLabels(Label selectedIdLabel, Label displayNameLabel, Holder<Enchantment> enchantment) {
        selectedIdLabel.setText(Component.literal("Enchantment ID: " + EnchantmentSearchBox.getEnchantmentIdString(enchantment)));
        displayNameLabel.setText(Component.literal("Display Name: " + (enchantment == null ? "" : enchantment.value().description().getString())));
    }

    private static void updateEnchantmentTagLabels(Label selectedTagIdLabel, Label selectedTagReferenceLabel, TagKey<Enchantment> tag) {
        selectedTagIdLabel.setText(Component.literal("Tag ID: " + EnchantmentTagSearchBox.getEnchantmentTagIdString(tag)));
        selectedTagReferenceLabel.setText(Component.literal("Tag Reference: " + EnchantmentTagSearchBox.getEnchantmentTagReferenceString(tag)));
    }

    private static void updateMobEffectLabels(Label selectedIdLabel, Label displayNameLabel, Holder<MobEffect> mobEffect) {
        selectedIdLabel.setText(Component.literal("Mob Effect ID: " + MobEffectSearchBox.getMobEffectIdString(mobEffect)));
        displayNameLabel.setText(Component.literal("Display Name: " + (mobEffect == null ? "" : mobEffect.value().getDisplayName().getString())));
    }

    private static void updateDimensionLabels(Label selectedIdLabel, Label selectedKeyLabel, ResourceKey<Level> dimension) {
        selectedIdLabel.setText(Component.literal("Dimension ID: " + DimensionSearchBox.getDimensionIdString(dimension)));
        selectedKeyLabel.setText(Component.literal("Resource Key: " + (dimension == null ? "" : dimension)));
    }

    private static void updateDataPackFileLabels(Label selectedIdLabel, Label pathPrefixLabel, DataPackFileSearchBox searchBox) {
        selectedIdLabel.setText(Component.literal("Data Pack File ID: " + searchBox.getSelectedFileIdString()));
        pathPrefixLabel.setText(Component.literal("Path Prefix: " + searchBox.getPathPrefix()));
    }

    private static void updatePostShaderLabels(Label selectedIdLabel, Label pathPrefixLabel, ResourcePackFileSearchBox searchBox) {
        selectedIdLabel.setText(Component.literal("Post Shader ID: " + searchBox.getSelectedFileIdString()));
        pathPrefixLabel.setText(Component.literal("Resource Path Prefix: " + searchBox.getPathPrefix()));
    }
}
