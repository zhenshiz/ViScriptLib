package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import org.jetbrains.annotations.Nullable;

/**
 * 客户端资源包 JSON 文件自动补全框，值类型为去掉 {@code .json} 后缀的 {@code ResourceLocation}。
 *
 * <p>例如路径前缀 {@code shaders/post} 会匹配
 * {@code assets/minecraft/shaders/post/invert.json}，选中值为
 * {@code minecraft:shaders/post/invert}。
 */
public class ResourcePackFileSearchBox extends JsonFileSearchBox {

    public ResourcePackFileSearchBox(String pathPrefix) {
        this(pathPrefix, null);
    }

    public ResourcePackFileSearchBox(String pathPrefix, @Nullable ResourceLocation defaultValue) {
        super(pathPrefix, defaultValue, ResourcePackFileSearchBox::getClientResourceManager);
    }

    public static void searchResourcePackJsonFiles(String pathPrefix, String word, IResultHandler<ResourceLocation> searchHandler) {
        searchJsonFiles(getClientResourceManager(), pathPrefix, word, searchHandler);
    }

    @Nullable
    static ResourceManager getClientResourceManager() {
        return Minecraft.getInstance().getResourceManager();
    }
}
