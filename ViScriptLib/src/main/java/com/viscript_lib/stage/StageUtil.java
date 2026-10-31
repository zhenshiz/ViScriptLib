package com.viscript_lib.stage;

import com.viscript_lib.ViScriptLibRegistries;
import lombok.experimental.UtilityClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** 读取共享阶段，或在服务端修改阶段并同步给所属玩家。 */
@UtilityClass
public class StageUtil {
    /**
     * 返回玩家当前的阶段快照。客户端读取所属玩家自己的服务端同步副本。
     *
     * @param player 玩家对象
     * @return 不可修改的阶段列表
     */
    public static List<String> getStageFlags(Player player) {
        return player.getData(ViScriptLibRegistries.STAGES).getFlags();
    }

    /**
     * 判断玩家是否拥有指定阶段。
     *
     * @param player 玩家对象
     * @param flag 阶段标记；忽略首尾空白，空白标记返回假
     * @return 是否拥有指定阶段
     */
    public static boolean hasStageFlag(Player player, String flag) {
        return flag != null && !flag.isBlank() && getStageFlags(player).contains(flag.trim());
    }

    /**
     * 判断玩家是否拥有全部指定阶段。
     *
     * @param player 玩家对象
     * @param flags 阶段集合；空集合或 {@code null} 不限制访问
     * @return 是否没有缺少的阶段
     */
    public static boolean hasStageFlags(Player player, Collection<String> flags) {
        return getMissingStageFlags(player, flags).isEmpty();
    }

    /**
     * 返回玩家缺少的非空阶段，按传入顺序去重。
     *
     * @param player 玩家对象
     * @param flags 所需阶段；空集合或 {@code null} 返回空列表
     * @return 缺少的阶段快照
     */
    public static List<String> getMissingStageFlags(Player player, Collection<String> flags) {
        if (flags == null || flags.isEmpty()) return List.of();
        var owned = getStageFlags(player);
        var missing = new LinkedHashSet<String>();
        for (String flag : flags) {
            if (flag != null && !flag.isBlank() && !owned.contains(flag.trim())) missing.add(flag.trim());
        }
        return List.copyOf(missing);
    }

    /**
     * 添加一个阶段并同步。
     *
     * @param player 服务端玩家
     * @param flag 阶段标记；忽略首尾空白，空白或已有标记不修改数据
     * @return 是否添加了阶段
     */
    public static boolean addStageFlag(ServerPlayer player, String flag) {
        return flag != null && addStageFlags(player, List.of(flag));
    }

    /**
     * 合并多个阶段并一次性同步，用于附属模组的数据迁移。
     *
     * @param player 服务端玩家
     * @param flags 待合并阶段；忽略空白、重复值和 {@code null}
     * @return 是否添加了至少一个阶段
     */
    public static boolean addStageFlags(ServerPlayer player, Collection<String> flags) {
        if (flags == null || flags.isEmpty()) return false;
        var current = getStageFlags(player);
        var merged = new ArrayList<>(current);
        merged.addAll(flags);
        var updated = PlayerStages.of(merged);
        if (updated.getFlags().equals(current)) return false;
        player.setData(ViScriptLibRegistries.STAGES, updated);
        return true;
    }

    /**
     * 移除一个阶段并同步。
     *
     * @param player 服务端玩家
     * @param flag 阶段标记；忽略首尾空白
     * @return 是否移除了阶段
     */
    public static boolean removeStageFlag(ServerPlayer player, String flag) {
        if (flag == null || flag.isBlank()) return false;
        var updated = new ArrayList<>(getStageFlags(player));
        if (!updated.remove(flag.trim())) return false;
        player.setData(ViScriptLibRegistries.STAGES, PlayerStages.of(updated));
        return true;
    }
}
