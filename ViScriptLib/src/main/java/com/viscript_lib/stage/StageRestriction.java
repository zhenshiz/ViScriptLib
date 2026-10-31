package com.viscript_lib.stage;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.ui.BooleanConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import com.viscript_lib.gui.configurator.StageRestrictionConfigurator;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 提供附属模组可直接持久化和编辑的阶段访问条件。 */
@Data
public class StageRestriction implements IPersistedSerializable, IConfigurable, StageRestricted<StageFlagGroup> {
    public static final Codec<StageRestriction> CODEC = PersistedParser.createCodec(StageRestriction::new);

    @Persisted private boolean stageRestrictionEnabled;
    @Persisted private StageMatchMode stageGroupMode = StageMatchMode.OR;
    @Persisted private List<StageFlagGroup> flagGroups = new ArrayList<>();
    @Persisted private List<String> lockMessages = new ArrayList<>();

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        father.addConfigurator(new BooleanConfigurator("viscript_lib.data.stage_restriction.enabled",
                this::isStageRestrictionEnabled, this::setStageRestrictionEnabled, false, true));
        father.addConfigurator(new StageRestrictionConfigurator<>(this, StageFlagGroup::new));
    }
}
