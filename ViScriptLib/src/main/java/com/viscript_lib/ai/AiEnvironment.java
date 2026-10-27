package com.viscript_lib.ai;

import com.viscript_lib.ViScriptLib;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 提供环境标识，并在服务端数据重载或关闭后使已有游标失效。
 */
public final class AiEnvironment {
    private static final String SESSION = UUID.randomUUID().toString();
    private static final AtomicLong REVISION = new AtomicLong();

    private AiEnvironment() {
    }

    /**
     * 返回当前进程和数据包状态的版本标识。
     *
     * @return 仅供比较的版本字符串
     */
    public static String revision() {
        return SESSION + ":" + REVISION.get();
    }

    /**
     * 创建不含凭据或玩家个人信息的运行环境快照。
     *
     * @return 包含协议版本、Minecraft 版本、VSL 版本、模组指纹和数据版本的快照
     */
    public static CompoundTag snapshot() {
        var data = new CompoundTag();
        data.putInt("protocol_version", 1);
        data.putString("minecraft_version", SharedConstants.getCurrentVersion().getName());
        data.putString("vsl_version", ModList.get().getModContainerById(ViScriptLib.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString()).orElse("unknown"));
        var mods = ModList.get().getMods().stream()
                .map(mod -> mod.getModId() + "=" + mod.getVersion()).sorted().collect(Collectors.joining("\n"));
        data.putString("mod_fingerprint", UUID.nameUUIDFromBytes(mods.getBytes(StandardCharsets.UTF_8)).toString());
        data.putString("registry_revision", revision());
        return data;
    }

    /**
     * 在服务端标签更新后递增数据版本。
     *
     * @param event 包含标签更新来源的事件
     */
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) REVISION.incrementAndGet();
    }

    /**
     * 在服务器关闭后使旧世界的游标失效。
     *
     * @param event 服务端关闭事件
     */
    public static void onServerStopped(ServerStoppedEvent event) {
        REVISION.incrementAndGet();
    }
}
