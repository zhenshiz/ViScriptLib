package com.viscript_lib.stage;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/** 定义阶段标记或条件组之间的逻辑关系。 */
public enum StageMatchMode implements StringRepresentable {
    AND, OR, NOT, NOR;

    /**
     * 返回用于选择组内标记关系的翻译键。
     *
     * @return 标记匹配方式的翻译键
     */
    public String getName() {
        return "viscript_lib.data.flag_group.mode." + name().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * 返回整个条件组合未满足时使用的提示翻译键。
     *
     * @return 条件组锁定提示的翻译键
     */
    public String getGroupLockKey() {
        return "viscript_lib.ui.flag_groups.lock.group_" + name().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * 返回用于选择条件组关系的翻译键。
     *
     * @return 条件组组合方式的翻译键
     */
    public String getGroupName() {
        return "viscript_lib.data.flag_group.group_mode." + name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
