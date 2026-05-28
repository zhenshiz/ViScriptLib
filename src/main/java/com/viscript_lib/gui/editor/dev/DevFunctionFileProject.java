package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.CodeEditor;
import com.lowdragmc.lowdraglib2.gui.ui.elements.codeeditor.language.Languages;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import dev.vfyjxf.taffy.style.FlexDirection;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * 开发环境无工程文件项目。
 */
public class DevFunctionFileProject implements IProject {
    @Getter
    private final Resources resources = Resources.of();
    @Getter
    private String content = "";
    private View view;
    private CodeEditor contentEditor;

    @Override
    public ProjectType getProjectType() {
        return DevFunctionFileProjectType.TYPE;
    }

    @Override
    public void initNewProject() {
        content = """
                {
                  "type": "function_file",
                  "message": "这是一个运行时文件和功能文件一体的测试文件。"
                }
                """;
    }

    @Override
    public CompoundTag serializeProject(@Nonnull HolderLookup.Provider provider) {
        return TagBuilder.compound()
                .add("type", "function_file")
                .add("content", content)
                .build();
    }

    @Override
    public void deserializeProject(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {
        content = nbt.getString("content");
    }

    @Override
    public void onLoad(Editor editor) {
        IProject.super.onLoad(editor);
        contentEditor = new CodeEditor();
        contentEditor.setLanguage(Languages.JAVASCRIPT);
        contentEditor.setValue(content.split("\n", -1), false);
        contentEditor.setLinesResponder(lines -> content = String.join("\n", lines));
        contentEditor.layout(layout -> layout.widthPercent(100).flex(1));

        view = new View("viscript_lib.dev_editor.function_file.view", Icons.JSON);
        view.layout(layout -> layout.flexDirection(FlexDirection.COLUMN).gapAll(2));
        view.addChildren(
                new Label()
                        .setText("viscript_lib.dev_editor.function_file.description")
                        .layout(layout -> layout.widthPercent(100).height(12)),
                contentEditor
        );
        editor.placeView(view, () -> editor.centerWindow.getLeftTop());
    }

    @Override
    public void onClosed(Editor editor) {
        IProject.super.onClosed(editor);
        if (view != null) {
            view.removeSelf();
        }
        view = null;
        contentEditor = null;
    }
}
