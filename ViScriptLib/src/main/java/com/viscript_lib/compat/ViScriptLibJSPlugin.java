package com.viscript_lib.compat;

import com.viscript_lib.stage.StageUtil;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;

/** 为客户端和服务端脚本提供共享阶段入口。 */
public class ViScriptLibJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        if (bindings.type() == ScriptType.SERVER || bindings.type() == ScriptType.CLIENT) {
            bindings.add("ViScriptStageUtil", StageUtil.class);
        }
    }
}
