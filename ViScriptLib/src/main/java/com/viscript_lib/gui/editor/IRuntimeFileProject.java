package com.viscript_lib.gui.editor;

import com.lowdragmc.lowdraglib2.editor.project.IProject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * 工程文件和运行时文件分离的项目需要提供运行时文件快照。
 */
public interface IRuntimeFileProject extends IProject {
    /**
     * 序列化运行时文件内容。
     *
     * @param provider 注册表访问器
     * @return 运行时文件 NBT
     */
    CompoundTag serializeRuntimeFile(@Nonnull HolderLookup.Provider provider);
}
