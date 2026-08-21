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
    private final MissingItemTestData missingItemTestData = new MissingItemTestData();
    private boolean writeMissingItemOnNextSave;
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
        missingItemTestData.reset();
        writeMissingItemOnNextSave = false;
    }

    @Override
    public CompoundTag serializeProject(@Nonnull HolderLookup.Provider provider) {
        var serializedItems = missingItemTestData.serializeNBT(provider);
        if (writeMissingItemOnNextSave) {
            writeMissingItemTestData(serializedItems);
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
            missingItemTestData.reset();
            serializedItems = missingItemTestData.serializeNBT(provider);
            var legacyItem = nbt.get("previewItem");
            if (legacyItem != null) {
                serializedItems.put("directItem", legacyItem.copy());
            }
        }
        missingItemTestData.deserializeNBT(provider, serializedItems);
        writeMissingItemOnNextSave = false;
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
                createMissingItemTestRow(),
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

    private UIElement createMissingItemTestRow() {
        var armButton = new Button().setText("viscript_lib.dev_editor.project_file.arm_missing_item");
        armButton.setOnClick(event -> {
            writeMissingItemOnNextSave = true;
            armButton.setText("viscript_lib.dev_editor.project_file.missing_item_armed");
        });
        armButton.layout(layout -> layout.height(16));

        return new UIElement()
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(20)
                        .flexDirection(FlexDirection.ROW)
                        .gapAll(4))
                .addChildren(
                        new Label().setText("viscript_lib.dev_editor.project_file.item_section")
                                .layout(layout -> layout.height(18)),
                        new ItemSlot().setItem(missingItemTestData.directItem),
                        new ItemSlot().setItem(missingItemTestData.firstCollectionItem()),
                        new ItemSlot().setItem(missingItemTestData.nested.item),
                        armButton
                );
    }

    private static void writeMissingItemTestData(CompoundTag serializedItems) {
        serializedItems.put("directItem", missingItem("missing_direct_item"));

        var collection = new ListTag();
        collection.add(missingItem("missing_collection_item"));
        serializedItems.put("collectionItems", collection);

        var nested = serializedItems.getCompound("nested").copy();
        nested.put("item", missingItem("missing_nested_item"));
        serializedItems.put("nested", nested);
    }

    private static CompoundTag missingItem(String path) {
        var missingItem = new CompoundTag();
        missingItem.putString("id", "viscript_lib:" + path);
        missingItem.putInt("count", 1);
        return missingItem;
    }

    private CodeEditor createEditor(String value, Consumer<String[]> responder) {
        var editor = new CodeEditor();
        editor.setLanguage(Languages.JAVASCRIPT);
        editor.setValue(value.split("\n", -1), false);
        editor.setLinesResponder(responder);
        editor.layout(layout -> layout.widthPercent(100).flex(1));
        return editor;
    }

    private static final class MissingItemTestData implements IPersistedSerializable {
        @Persisted
        private ItemStack directItem = ItemStack.EMPTY;
        @Persisted
        private final List<ItemStack> collectionItems = new ArrayList<>();
        @Persisted(subPersisted = true)
        private final NestedItemData nested = new NestedItemData();

        private void reset() {
            directItem = new ItemStack(Items.DIAMOND);
            collectionItems.clear();
            collectionItems.add(new ItemStack(Items.EMERALD));
            nested.item = new ItemStack(Items.GOLD_INGOT);
        }

        private ItemStack firstCollectionItem() {
            return collectionItems.isEmpty() ? ItemStack.EMPTY : collectionItems.get(0);
        }
    }

    private static final class NestedItemData {
        @Persisted
        private ItemStack item = ItemStack.EMPTY;
    }
}
