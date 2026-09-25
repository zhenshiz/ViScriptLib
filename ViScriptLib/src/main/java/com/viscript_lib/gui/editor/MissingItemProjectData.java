package com.viscript_lib.gui.editor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 在已序列化的编辑器项目中查找和删除缺失物品 ID。
 */
final class MissingItemProjectData {
    private static final Set<String> ITEM_STACK_FIELDS = Set.of("id", "count", "components");

    private MissingItemProjectData() {
    }

    /**
     * 查找项目数据中使用的全部未注册物品 ID。
     *
     * @param  projectData 已序列化的完整项目数据
     * @return 按完整资源位置排序且不重复的缺失物品 ID
     */
    static List<ResourceLocation> findMissingItemIds(CompoundTag projectData) {
        var result = new LinkedHashSet<ResourceLocation>();
        collectMissingItemIds(projectData, result);
        return result.stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
    }

    /**
     * 删除项目数据中使用指定未注册物品 ID 的所有物品栈。
     *
     * <p>列表中的匹配项会被移除；复合标签字段中的匹配项会替换为空复合标签，以便
     * <code>ViScriptItemStack</code> Codec 将其读取为空物品栈。已注册物品 ID 不会被删除。
     *
     * @param  projectData 需要修改的完整项目数据
     * @param  itemId 需要删除的未注册物品 ID
     * @return 删除或清空的物品栈条目数量
     */
    static int deleteMissingItemId(CompoundTag projectData, ResourceLocation itemId) {
        if (BuiltInRegistries.ITEM.containsKey(itemId)) {
            return 0;
        }
        return deleteFromCompound(projectData, itemId);
    }

    private static void collectMissingItemIds(Tag tag, Set<ResourceLocation> result) {
        if (tag instanceof CompoundTag compound) {
            getMissingItemId(compound).ifPresent(result::add);
            for (var key : compound.getAllKeys()) {
                var child = compound.get(key);
                if (child != null) {
                    collectMissingItemIds(child, result);
                }
            }
        } else if (tag instanceof ListTag list) {
            for (var child : list) {
                collectMissingItemIds(child, result);
            }
        }
    }

    private static int deleteFromCompound(CompoundTag compound, ResourceLocation itemId) {
        var removed = 0;
        for (var key : new ArrayList<>(compound.getAllKeys())) {
            var child = compound.get(key);
            if (child instanceof CompoundTag childCompound) {
                if (isMatchingMissingItem(childCompound, itemId)) {
                    compound.put(key, new CompoundTag());
                    removed++;
                } else {
                    removed += deleteFromCompound(childCompound, itemId);
                }
            } else if (child instanceof ListTag list) {
                removed += deleteFromList(list, itemId);
            }
        }
        return removed;
    }

    private static int deleteFromList(ListTag list, ResourceLocation itemId) {
        var removed = 0;
        for (var index = list.size() - 1; index >= 0; index--) {
            var child = list.get(index);
            if (child instanceof CompoundTag compound && isMatchingMissingItem(compound, itemId)) {
                list.remove(index);
                removed++;
            } else if (child instanceof CompoundTag compound) {
                removed += deleteFromCompound(compound, itemId);
            } else if (child instanceof ListTag nestedList) {
                removed += deleteFromList(nestedList, itemId);
            }
        }
        return removed;
    }

    private static boolean isMatchingMissingItem(CompoundTag compound, ResourceLocation itemId) {
        return getMissingItemId(compound).map(itemId::equals).orElse(false);
    }

    private static java.util.Optional<ResourceLocation> getMissingItemId(CompoundTag compound) {
        if (!compound.contains("id", Tag.TAG_STRING)
                || !compound.contains("count", Tag.TAG_ANY_NUMERIC)
                || compound.getInt("count") <= 0
                || !ITEM_STACK_FIELDS.containsAll(compound.getAllKeys())) {
            return java.util.Optional.empty();
        }
        if (compound.contains("components")
                && !compound.contains("components", Tag.TAG_COMPOUND)) {
            return java.util.Optional.empty();
        }

        var itemId = ResourceLocation.tryParse(compound.getString("id"));
        return itemId != null && !BuiltInRegistries.ITEM.containsKey(itemId)
                ? java.util.Optional.of(itemId)
                : java.util.Optional.empty();
    }
}
