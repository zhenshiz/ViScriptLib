package com.viscript_lib.stage;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** 为自有存档格式提供共用的阶段条件组判断。 */
public interface IStageFlagGroup {
    /**
     * 返回本组标记之间的逻辑关系。
     *
     * @return 匹配方式；{@code null} 按 {@link StageMatchMode#AND} 处理
     */
    StageMatchMode getStageMatchMode();

    /**
     * 更新本组标记之间的逻辑关系。
     *
     * @param mode 匹配方式
     */
    void setStageMatchMode(StageMatchMode mode);

    /**
     * 返回此组保存的原始阶段标记。
     *
     * @return 原始标记列表；允许 {@code null}
     */
    List<String> getFlags();

    /**
     * 替换此组保存的阶段标记。
     *
     * @param flags 原始标记列表
     */
    void setFlags(List<String> flags);

    /**
     * 返回去重并去除首尾空白的阶段标记。
     *
     * @return 保持原始顺序的独立列表，不包含空白和 {@code null}
     */
    default List<String> normalizedFlags() {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (getFlags() != null) {
            for (String flag : getFlags()) {
                String value = flag == null ? "" : flag.trim();
                if (!value.isEmpty()) {
                    normalized.add(value);
                }
            }
        }
        return new ArrayList<>(normalized);
    }

    /**
     * 判断此组是否没有有效阶段标记。
     *
     * @return 没有有效标记时返回 {@code true}
     */
    default boolean isEmpty() {
        return normalizedFlags().isEmpty();
    }

    /**
     * 判断玩家阶段是否满足此组条件。
     *
     * @param playerFlags 玩家阶段；{@code null} 按空集合处理
     * @return 空组或条件满足时返回 {@code true}
     */
    default boolean matches(Collection<String> playerFlags) {
        Collection<String> activeFlags = playerFlags == null ? List.of() : playerFlags;
        List<String> required = normalizedFlags();
        if (required.isEmpty()) {
            return true;
        }

        StageMatchMode activeMode = getStageMatchMode() == null ? StageMatchMode.AND : getStageMatchMode();
        return switch (activeMode) {
            case AND -> activeFlags.containsAll(required);
            case OR -> required.stream().anyMatch(activeFlags::contains);
            case NOT -> !activeFlags.containsAll(required);
            case NOR -> required.stream().noneMatch(activeFlags::contains);
        };
    }

    /**
     * 创建此组的缺失或冲突标记提示。
     *
     * @param index 提示中显示的条件组序号
     * @param playerFlags 玩家阶段；{@code null} 按空集合处理
     * @return 可在客户端翻译的提示文本
     */
    default Component getLockHint(int index, Collection<String> playerFlags) {
        Collection<String> activeFlags = playerFlags == null ? List.of() : playerFlags;
        List<String> required = normalizedFlags();
        StageMatchMode activeMode = getStageMatchMode() == null ? StageMatchMode.AND : getStageMatchMode();
        return switch (activeMode) {
            case AND -> Component.translatable("viscript_lib.ui.flag_groups.lock.and", index, join(missingFlags(required, activeFlags)));
            case OR -> Component.translatable("viscript_lib.ui.flag_groups.lock.or", index, join(required));
            case NOT -> Component.translatable("viscript_lib.ui.flag_groups.lock.not", index, join(required));
            case NOR -> Component.translatable("viscript_lib.ui.flag_groups.lock.nor", index, join(conflictingFlags(required, activeFlags)));
        };
    }

    /**
     * 按组关系判断完整的阶段条件。
     *
     * @param groupMode 条件组关系；{@code null} 按 {@link StageMatchMode#OR} 处理
     * @param groups 条件组列表；空组和 {@code null} 组被忽略
     * @param playerFlags 玩家阶段；{@code null} 按空集合处理
     * @return 没有有效条件组或组合条件满足时返回 {@code true}
     */
    static boolean canAccess(StageMatchMode groupMode, List<? extends IStageFlagGroup> groups, Collection<String> playerFlags) {
        Collection<String> activeFlags = playerFlags == null ? List.of() : playerFlags;
        List<? extends IStageFlagGroup> activeGroups = activeGroups(groups);
        if (activeGroups.isEmpty()) {
            return true;
        }
        StageMatchMode activeMode = groupMode == null ? StageMatchMode.OR : groupMode;
        return switch (activeMode) {
            case AND -> activeGroups.stream().allMatch(group -> group.matches(activeFlags));
            case OR -> activeGroups.stream().anyMatch(group -> group.matches(activeFlags));
            case NOT -> !activeGroups.stream().allMatch(group -> group.matches(activeFlags));
            case NOR -> activeGroups.stream().noneMatch(group -> group.matches(activeFlags));
        };
    }

    /**
     * 返回锁定原因，包含总规则及至多四组详细提示。
     *
     * @param groupMode 条件组关系；{@code null} 按 {@link StageMatchMode#OR} 处理
     * @param groups 条件组列表
     * @param playerFlags 玩家阶段；{@code null} 按空集合处理
     * @return 已解锁时为空；超出四组时包含剩余组数提示
     */
    static List<Component> getLockTooltips(StageMatchMode groupMode, List<? extends IStageFlagGroup> groups, Collection<String> playerFlags) {
        Collection<String> activeFlags = playerFlags == null ? List.of() : playerFlags;
        List<? extends IStageFlagGroup> activeGroups = activeGroups(groups);
        if (activeGroups.isEmpty() || canAccess(groupMode, activeGroups, playerFlags)) {
            return List.of();
        }

        List<Component> tooltips = new ArrayList<>();
        StageMatchMode activeMode = groupMode == null ? StageMatchMode.OR : groupMode;
        tooltips.add(Component.translatable(activeMode.getGroupLockKey()));
        List<Component> details = new ArrayList<>();
        for (int i = 0; i < activeGroups.size(); i++) {
            IStageFlagGroup group = activeGroups.get(i);
            boolean matched = group.matches(activeFlags);
            if (activeMode == StageMatchMode.NOT || activeMode == StageMatchMode.NOR) {
                if (matched) {
                    details.add(Component.translatable("viscript_lib.ui.flag_groups.lock.satisfied", i + 1));
                }
            } else if (!matched) {
                details.add(group.getLockHint(i + 1, activeFlags));
            }
        }

        int maxShown = Math.min(4, details.size());
        for (int i = 0; i < maxShown; i++) {
            tooltips.add(details.get(i));
        }
        if (details.size() > maxShown) {
            tooltips.add(Component.translatable("viscript_lib.ui.flag_groups.lock.more", details.size() - maxShown));
        }
        return tooltips;
    }

    private static List<? extends IStageFlagGroup> activeGroups(List<? extends IStageFlagGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }
        return groups.stream()
                .filter(group -> group != null && !group.isEmpty())
                .toList();
    }

    private static List<String> missingFlags(List<String> required, Collection<String> playerFlags) {
        return required.stream()
                .filter(flag -> !playerFlags.contains(flag))
                .toList();
    }

    private static List<String> conflictingFlags(List<String> required, Collection<String> playerFlags) {
        return required.stream()
                .filter(playerFlags::contains)
                .toList();
    }

    private static String join(List<String> flags) {
        return String.join(", ", flags);
    }

}
