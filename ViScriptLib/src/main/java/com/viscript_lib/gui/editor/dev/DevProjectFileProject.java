package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.CodeEditor;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.language.Languages;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import com.viscript_lib.gui.editor.IRuntimeFileProject;
import com.viscript_lib.util.item.ViScriptItemStack;
import dev.vfyjxf.taffy.style.FlexDirection;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 开发环境工程文件项目。
 *
 * <p><code>projectNote</code> 用于模拟只存在于工程文件的编辑器数据，
 * <code>runtimeContent</code> 用于生成运行时文件。
 */
public class DevProjectFileProject implements IRuntimeFileProject {
    @Getter
    private final Resources resources = Resources.of();
    private String projectNote = "";
    private String runtimeContent = "";
    private final UnavailableItemTestData unavailableItemTestData = new UnavailableItemTestData();
    private boolean writeUnavailableItemsOnNextSave;
    private View view;
    private CodeEditor projectEditor;
    private CodeEditor runtimeEditor;

    @Override
    public ProjectType getProjectType() {
        return DevProjectFileProjectType.TYPE;
    }

    @Override
    public void initNewProject() {
        projectNote = """
                {
                  "editor_only": "这是只保存在工程文件里的 UI/编辑器数据。"
                }
                """;
        runtimeContent = """
                {
                  "type": "project_runtime_file",
                  "message": "这是由工程文件导出的运行时文件。"
                }
                """;
        unavailableItemTestData.reset();
        writeUnavailableItemsOnNextSave = false;
    }

    @Override
    public CompoundTag serializeProject(@Nonnull HolderLookup.Provider provider) {
        var serializedItems = unavailableItemTestData.serializeNBT(provider);
        if (writeUnavailableItemsOnNextSave) {
            writeUnavailableItemTestData(serializedItems);
        }
        return TagBuilder.compound()
                .add("type", "project_file")
                .add("projectNote", projectNote)
                .add("runtimeContent", runtimeContent)
                .add("itemTestData", serializedItems)
                .build();
    }

    @Override
    public void deserializeProject(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {
        projectNote = nbt.getString("projectNote");
        runtimeContent = nbt.getString("runtimeContent");
        var serializedItems = nbt.getCompound("itemTestData").copy();
        if (serializedItems.isEmpty()) {
            unavailableItemTestData.reset();
            serializedItems = unavailableItemTestData.serializeNBT(provider);
            var legacyItem = nbt.get("previewItem");
            if (legacyItem != null) {
                serializedItems.put("directItem", legacyItem.copy());
            }
        }
        migrateLegacyUnavailableItemTestData(serializedItems);
        unavailableItemTestData.deserializeNBT(provider, serializedItems);
        writeUnavailableItemsOnNextSave = false;
    }

    @Override
    public CompoundTag serializeRuntimeFile(@Nonnull HolderLookup.Provider provider) {
        return TagBuilder.compound()
                .add("type", "project_runtime_file")
                .add("content", runtimeContent)
                .build();
    }

    @Override
    public void onLoad(Editor editor) {
        projectEditor = createEditor(projectNote, lines -> projectNote = String.join("\n", lines));
        runtimeEditor = createEditor(runtimeContent, lines -> runtimeContent = String.join("\n", lines));

        view = new View("viscript_lib.dev_editor.project_file.view", Icons.JSON);
        view.layout(layout -> layout.flexDirection(FlexDirection.COLUMN).gapAll(2));
        view.addChildren(
                createUnavailableItemTestSection(),
                new Label()
                        .setText("viscript_lib.dev_editor.project_file.project_section")
                        .layout(layout -> layout.widthPercent(100).height(12)),
                projectEditor,
                new Label()
                        .setText("viscript_lib.dev_editor.project_file.runtime_section")
                        .layout(layout -> layout.widthPercent(100).height(12)),
                runtimeEditor
        );
        editor.placeView(view, () -> editor.centerWindow.getLeftTop());
    }

    @Override
    public void onClosed(Editor editor) {
        if (view != null) {
            view.removeSelf();
        }
        view = null;
        projectEditor = null;
        runtimeEditor = null;
    }

    private UIElement createUnavailableItemTestSection() {
        var armButton = new Button().setText("viscript_lib.dev_editor.project_file.arm_unavailable_items");
        armButton.setOnClick(event -> {
            writeUnavailableItemsOnNextSave = true;
            armButton.setText("viscript_lib.dev_editor.project_file.unavailable_items_armed");
        });
        armButton.layout(layout -> layout.height(16));

        return new UIElement()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(84)
                        .flexDirection(FlexDirection.COLUMN)
                        .gapAll(2))
                .addChildren(
                        new UIElement()
                                .layout(layout -> layout
                                        .widthPercent(100)
                                        .height(18)
                                        .flexDirection(FlexDirection.ROW)
                                        .gapAll(4))
                                .addChildren(
                                        new Label().setText("viscript_lib.dev_editor.project_file.item_section")
                                                .layout(layout -> layout.height(18).flex(1)),
                                        armButton
                                ),
                        createUnavailableItemCase(
                                "viscript_lib.dev_editor.project_file.case_missing_item",
                                unavailableItemTestData.directItem.toItemStack()
                        ),
                        createUnavailableItemCase(
                                "viscript_lib.dev_editor.project_file.case_missing_component",
                                unavailableItemTestData.firstCollectionItem()
                        ),
                        createUnavailableItemCase(
                                "viscript_lib.dev_editor.project_file.case_missing_enchantment",
                                unavailableItemTestData.nested.item.toItemStack()
                        )
                );
    }

    private static UIElement createUnavailableItemCase(String labelKey, ItemStack itemStack) {
        return new UIElement()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(20)
                        .flexDirection(FlexDirection.ROW)
                        .gapAll(4))
                .addChildren(
                        new ItemSlot().setItem(itemStack),
                        new Label().setText(labelKey).layout(layout -> layout.height(18))
                );
    }

    private static void writeUnavailableItemTestData(CompoundTag serializedItems) {
        serializedItems.put("directItem", missingItem());

        var collection = new ListTag();
        collection.add(missingComponent());
        serializedItems.put("collectionItems", collection);

        var nested = serializedItems.getCompound("nested").copy();
        nested.put("item", missingEnchantment());
        serializedItems.put("nested", nested);
    }

    static boolean migrateLegacyUnavailableItemTestData(CompoundTag serializedItems) {
        var directItem = serializedItems.getCompound("directItem");
        if (!directItem.getString("id").equals("viscript_lib:missing_direct_item")) {
            return false;
        }
        writeUnavailableItemTestData(serializedItems);
        return true;
    }

    private static CompoundTag missingItem() {
        var missingItem = new CompoundTag();
        missingItem.putString("id", "viscript_lib:missing_item");
        missingItem.putInt("count", 1);
        return missingItem;
    }

    private static CompoundTag missingComponent() {
        var potionContents = new CompoundTag();
        potionContents.putString("potion", "minecraft:water");
        var components = new CompoundTag();
        components.put("minecraft:potion_contents", potionContents);
        components.putInt("viscript_lib:missing_component", 3);

        var item = new CompoundTag();
        item.putString("id", "minecraft:potion");
        item.putInt("count", 1);
        item.put("components", components);
        return item;
    }

    private static CompoundTag missingEnchantment() {
        var levels = new CompoundTag();
        levels.putInt("minecraft:sharpness", 2);
        levels.putInt("viscript_lib:missing_enchantment", 3);
        var enchantments = new CompoundTag();
        enchantments.put("levels", levels);
        var components = new CompoundTag();
        components.put("minecraft:enchantments", enchantments);

        var item = new CompoundTag();
        item.putString("id", "minecraft:diamond_sword");
        item.putInt("count", 1);
        item.put("components", components);
        return item;
    }

    private CodeEditor createEditor(String value, Consumer<String[]> responder) {
        var editor = new CodeEditor();
        editor.setLanguage(Languages.JAVASCRIPT);
        editor.setValue(value.split("\n", -1), false);
        editor.setLinesResponder(responder);
        editor.layout(layout -> layout.widthPercent(100).flex(1));
        return editor;
    }

    private static final class UnavailableItemTestData implements IPersistedSerializable {
        @Persisted
        private ViScriptItemStack directItem = new ViScriptItemStack();
        @Persisted
        private final List<ViScriptItemStack> collectionItems = new ArrayList<>();
        @Persisted(subPersisted = true)
        private final NestedItemData nested = new NestedItemData();

        private void reset() {
            directItem = new ViScriptItemStack(new ItemStack(Items.DIAMOND));
            collectionItems.clear();
            collectionItems.add(new ViScriptItemStack(new ItemStack(Items.EMERALD)));
            nested.item = new ViScriptItemStack(new ItemStack(Items.GOLD_INGOT));
        }

        private ItemStack firstCollectionItem() {
            return collectionItems.isEmpty() ? ItemStack.EMPTY : collectionItems.get(0).toItemStack();
        }
    }

    private static final class NestedItemData {
        @Persisted
        private ViScriptItemStack item = new ViScriptItemStack();
    }
}
