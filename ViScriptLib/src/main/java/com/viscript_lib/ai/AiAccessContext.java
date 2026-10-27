package com.viscript_lib.ai;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * 携带由服务端创建的调用者身份、世界和已授权能力。
 *
 * <p>此对象不能从客户端请求或模型输出反序列化。工厂方法仅为具有二级及以上命令权限
 * 的调用者授予注册表读取能力；编辑能力必须由可信服务端流程另行授权。
 * 编辑器本地偏好不构成服务端授权。持有方应在服务端线程内完成查询。
 *
 * @param player 发起请求的玩家；控制台请求为 {@code null}
 * @param level 请求所在的服务端世界
 * @param permissionLevel 调用者的命令权限等级，范围为 {@code 0} 至 {@code 4}
 * @param capabilities 服务端授予的能力集合
 */
public record AiAccessContext(@Nullable ServerPlayer player, ServerLevel level,
                              int permissionLevel, Set<AiCapability> capabilities) {
    public AiAccessContext {
        java.util.Objects.requireNonNull(level, "level");
        permissionLevel = Math.clamp(permissionLevel, 0, 4);
        capabilities = Set.copyOf(capabilities);
        if (player != null && player.serverLevel().getServer() != level.getServer()) {
            throw new IllegalArgumentException("Player and level must belong to the same server");
        }
    }

    /**
     * 根据命令来源创建只读上下文。
     *
     * @param source 服务端命令来源
     * @return 此来源当前允许使用的能力上下文
     */
    public static AiAccessContext forSource(CommandSourceStack source) {
        int permission = 0;
        for (int candidate = 1; candidate <= 4; candidate++) {
            if (source.hasPermission(candidate)) permission = candidate;
        }
        var capabilities = EnumSet.noneOf(AiCapability.class);
        if (permission >= 2) {
            capabilities.add(AiCapability.REGISTRY_READ);
        }
        return new AiAccessContext(source.getPlayer(), source.getLevel(), permission, capabilities);
    }

    /**
     * 根据玩家当前权限创建上下文。
     *
     * @param player 发起请求的服务端玩家
     * @return 当前玩家的只读权限上下文
     */
    public static AiAccessContext forPlayer(ServerPlayer player) {
        return forSource(player.createCommandSourceStack());
    }

    /**
     * 根据服务器控制台权限创建指定世界的上下文。
     *
     * @param level 请求所在的服务端世界
     * @return 当前服务器控制台的只读权限上下文
     */
    public static AiAccessContext forServer(ServerLevel level) {
        return forSource(level.getServer().createCommandSourceStack().withLevel(level));
    }

    /**
     * 检查调用者当前权限和服务端授予的能力是否都允许操作。
     *
     * @param capability 要检查的能力
     * @return 所有条件均满足时为 {@code true}
     */
    public boolean allows(AiCapability capability) {
        if (capability == null || !capabilities.contains(capability) || permissionLevel < 2) return false;
        if (player != null && (player.connection == null || !player.connection.isAcceptingMessages()
                || !player.hasPermissions(2))) return false;
        return true;
    }
}
