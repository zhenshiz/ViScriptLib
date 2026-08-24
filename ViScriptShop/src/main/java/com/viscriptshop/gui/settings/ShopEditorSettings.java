package com.viscriptshop.gui.settings;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.DefaultValue;
import com.lowdragmc.lowdraglib2.editor.settings.Settings;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import com.viscriptshop.ViscriptShop;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

/**
 * 保存商店编辑器的上传行为设置。
 */
@Getter
@Setter
public final class ShopEditorSettings implements Settings {
    public static final ResourceLocation ID = ViscriptShop.id("editor_upload");
    public static final Codec<ShopEditorSettings> CODEC = PersistedParser.createCodec(ShopEditorSettings::new);

    @DefaultValue(booleanValue = true)
    @Configurable(
            name = "settings.viscript_shop.editor_upload.reloadShopAfterUpload",
            tips = "settings.viscript_shop.editor_upload.reloadShopAfterUpload.tip"
    )
    private boolean reloadShopAfterUpload = true;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public String getPath() {
        return "Behavior";
    }

    @Override
    public void onApply(Editor editor) {
    }
}
