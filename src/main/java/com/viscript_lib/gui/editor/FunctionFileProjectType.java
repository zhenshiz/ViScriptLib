package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;

import java.io.File;
import java.util.function.Supplier;

/**
 * 运行时文件和功能文件一体的项目类型。
 *
 * <p>此项目类型把 LDLib2 的项目后缀设置为运行时文件后缀，并把另存为根目录
 * 指向运行时文件目录。
 */
public class FunctionFileProjectType extends ProjectType {
    private final EditorFileFormat format;

    /**
     * 创建运行时文件项目类型。
     *
     * @param icon 菜单图标
     * @param name 项目类型翻译键
     * @param format 文件格式定义
     * @param projectCreator 项目构造器
     */
    public FunctionFileProjectType(IGuiTexture icon, String name, EditorFileFormat format, Supplier<IProject> projectCreator) {
        super(icon, name, format.runtimeSuffix(), projectCreator);
        this.format = format;
    }

    /**
     * 返回文件格式定义。
     *
     * @return 文件格式定义
     */
    public EditorFileFormat getFormat() {
        return format;
    }

    /**
     * 返回运行时文件保存根目录。
     *
     * @param project 当前项目
     * @param projectRoot LDLib2 默认项目根目录
     * @return 运行时文件目录
     */
    @Override
    public File getRootSavePath(IProject project, File projectRoot) {
        return format.functionDirectory();
    }
}
