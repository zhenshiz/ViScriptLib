package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * 数据包 JSON 文件自动补全框，值类型为去掉 {@code .json} 后缀的 {@code ResourceLocation}。
 *
 * <p>例如路径前缀 {@code chatbox/dialogues} 会匹配
 * {@code data/test/chatbox/dialogues/example.json}，选中值为
 * {@code test:chatbox/dialogues/example}。
 */
public class DataPackFileSearchBox extends JsonFileSearchBox {

    public DataPackFileSearchBox(String pathPrefix) {
        this(pathPrefix, null);
    }

    public DataPackFileSearchBox(String pathPrefix, @Nullable ResourceLocation defaultValue) {
        super(pathPrefix, defaultValue, DataPackFileSearchBox::getServerResourceManager);
    }

    public DataPackFileSearchBox searchOnServer() {
        setSearchOnServer(ResourceLocation[].class);
        return this;
    }

    public static void searchDataPackJsonFiles(String pathPrefix, String word, IResultHandler<ResourceLocation> searchHandler) {
        searchJsonFiles(getServerResourceManager(), pathPrefix, word, searchHandler);
    }

    @Nullable
    static ResourceManager getServerResourceManager() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getResourceManager();
    }
}
