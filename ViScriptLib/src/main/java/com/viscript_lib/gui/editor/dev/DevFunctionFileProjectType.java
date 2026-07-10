package com.viscript_lib.gui.editor.dev;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.viscript_lib.ViScriptLib;
import com.viscript_lib.gui.editor.EditorFileFormat;
import com.viscript_lib.gui.editor.FunctionFileProjectType;

/**
 * 开发环境无工程文件编辑器的测试项目类型。
 */
public final class DevFunctionFileProjectType {
    public static final EditorFileFormat FORMAT = EditorFileFormat.of(ViScriptLib.MOD_ID, "test", "test");
    public static final FunctionFileProjectType TYPE = new FunctionFileProjectType(
            Icons.JSON,
            "viscript_lib.dev_editor.function_file.type",
            FORMAT,
            DevFunctionFileProject::new
    );
}
