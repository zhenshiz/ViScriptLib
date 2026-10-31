package com.viscript_lib.compat;

import com.viscript_lib.stage.StageUtil;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;

/** 为客户端和服务端脚本提供共享阶段入口。 */
public class ViScriptLibJSPlugin extends KubeJSPlugin {
    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.getType() == ScriptType.SERVER || event.getType() == ScriptType.CLIENT) {
            event.add("ViScriptStageUtil", StageUtil.class);
        }
    }
}
