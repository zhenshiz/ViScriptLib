package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.editor.EditorFileFormat;
import com.viscript_lib.gui.editor.ProjectFileProjectType;

/**
 * 开发环境工程文件编辑器的测试项目类型。
 */
public final class DevProjectFileProjectType {
    public static final EditorFileFormat FORMAT = EditorFileFormat.of(ViScriptLib.MOD_ID, "test", "ptest");
    public static final ProjectFileProjectType TYPE = new ProjectFileProjectType(
            Icons.JSON,
            "viscript_lib.dev_editor.project_file.type",
            FORMAT,
            DevProjectFileProject::new
    );
}
