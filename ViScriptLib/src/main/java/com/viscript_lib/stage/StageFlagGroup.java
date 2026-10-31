package com.viscript_lib.stage;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** 保存一组阶段标记及其匹配方式。空组不限制访问。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageFlagGroup implements IPersistedSerializable, IStageFlagGroup {
    public static final Codec<StageFlagGroup> CODEC = PersistedParser.createCodec(StageFlagGroup::new);

    @Persisted
    private StageMatchMode stageMatchMode = StageMatchMode.AND;
    @Persisted
    private List<String> flags = new ArrayList<>();
}
