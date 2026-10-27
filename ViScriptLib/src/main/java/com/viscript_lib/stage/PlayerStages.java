package com.viscript_lib.stage;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** 保存玩家的共享阶段标记，修改通过 {@link StageUtil} 提交并同步。 */
public final class PlayerStages implements IPersistedSerializable {
    public static final Codec<PlayerStages> CODEC = PersistedParser.createCodec(PlayerStages::new);
    public static final StreamCodec<ByteBuf, PlayerStages> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    @Persisted
    private List<String> flags = new ArrayList<>();

    /**
     * 返回阶段标记的不可修改快照。
     *
     * @return 已去重的阶段标记；空白值不包含在结果中
     */
    public List<String> getFlags() {
        return List.copyOf(normalize(flags));
    }

    static PlayerStages of(Collection<String> flags) {
        var data = new PlayerStages();
        data.flags = normalize(flags);
        return data;
    }

    private static ArrayList<String> normalize(Collection<String> flags) {
        var result = new LinkedHashSet<String>();
        if (flags != null) {
            for (String flag : flags) {
                if (flag != null && !flag.isBlank()) result.add(flag.trim());
            }
        }
        return new ArrayList<>(result);
    }
}
