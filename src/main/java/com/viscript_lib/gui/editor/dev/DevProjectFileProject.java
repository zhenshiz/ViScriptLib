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
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import com.viscript_lib.gui.editor.IRuntimeFileProject;
import com.viscript_lib.util.item.MissingItemStackRecovery;
import dev.vfyjxf.taffy.style.FlexDirection;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
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
    private ItemStack previewItem = ItemStack.EMPTY;
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
        previewItem = new ItemStack(Items.DIAMOND);
        writeMissingItemOnNextSave = false;
    }

    @Override
    public CompoundTag serializeProject(@Nonnull HolderLookup.Provider provider) {
        return TagBuilder.compound()
                .add("type", "project_file")
                .add("projectNote", projectNote)
                .add("runtimeContent", runtimeContent)
                .add("previewItem", serializePreviewItem(provider))
                .build();
    }

    @Override
    public void deserializeProject(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {
        projectNote = nbt.getString("projectNote");
        runtimeContent = nbt.getString("runtimeContent");
        var serializedItem = nbt.get("previewItem");
        previewItem = serializedItem == null
                ? new ItemStack(Items.DIAMOND)
                : MissingItemStackRecovery.CODEC.parse(
                        provider.createSerializationContext(NbtOps.INSTANCE),
                        serializedItem
                ).getOrThrow();
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
                        new ItemSlot().setItem(previewItem),
                        armButton
                );
    }

    private Tag serializePreviewItem(HolderLookup.Provider provider) {
        if (writeMissingItemOnNextSave) {
            var missingItem = new CompoundTag();
            missingItem.putString("id", "viscript_lib:missing_item_recovery_test");
            missingItem.putInt("count", 1);
            return missingItem;
        }
        return MissingItemStackRecovery.CODEC.encodeStart(
                provider.createSerializationContext(NbtOps.INSTANCE),
                previewItem
        ).getOrThrow();
    }

    private CodeEditor createEditor(String value, Consumer<String[]> responder) {
        var editor = new CodeEditor();
        editor.setLanguage(Languages.JAVASCRIPT);
        editor.setValue(value.split("\n", -1), false);
        editor.setLinesResponder(responder);
        editor.layout(layout -> layout.widthPercent(100).flex(1));
        return editor;
    }
}
