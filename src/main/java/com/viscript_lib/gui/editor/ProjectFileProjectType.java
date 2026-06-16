package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import lombok.Getter;

import java.io.File;
import java.util.function.Supplier;

/**
 * 工程文件和运行时文件分离的项目类型。
 *
 * <p>工程文件统一存放到 <code>assets/&lt;modid&gt;/project</code>，文件筛选
 * 依赖由运行时后缀派生出的工程后缀。
 */
public class ProjectFileProjectType extends ProjectType {
    @Getter
    private final EditorFileFormat format;

    /**
     * 创建工程文件项目类型。
     *
     * @param icon 菜单图标
     * @param name 项目类型翻译键
     * @param format 文件格式定义
     * @param projectCreator 项目构造器
     */
    public ProjectFileProjectType(IGuiTexture icon, String name, EditorFileFormat format,
                                  Supplier<? extends IRuntimeFileProject> projectCreator) {
        super(icon, name, format.projectSuffix(), projectCreator::get);
        this.format = format;
    }

    /**
     * 返回工程文件保存根目录。
     *
     * @param project 当前项目
     * @param projectRoot LDLib2 默认项目根目录
     * @return 工程文件目录
     */
    @Override
    public File getRootSavePath(IProject project, File projectRoot) {
        return format.projectDirectory();
    }
}
