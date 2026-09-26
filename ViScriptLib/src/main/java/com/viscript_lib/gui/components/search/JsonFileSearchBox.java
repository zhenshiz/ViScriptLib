package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * JSON 资源文件自动补全框，值类型为去掉 {@code .json} 后缀的 {@code ResourceLocation}。
 */
public class JsonFileSearchBox extends SearchComponent<ResourceLocation> {
    private static final String JSON_SUFFIX = ".json";

    private final Supplier<@Nullable ResourceManager> resourceManagerSupplier;
    @Getter
    private String pathPrefix;

    public JsonFileSearchBox(String pathPrefix, Supplier<@Nullable ResourceManager> resourceManagerSupplier) {
        this(pathPrefix, null, resourceManagerSupplier);
    }

    public JsonFileSearchBox(String pathPrefix,
                             @Nullable ResourceLocation defaultValue,
                             Supplier<@Nullable ResourceManager> resourceManagerSupplier) {
        super();
        this.pathPrefix = normalizePathPrefix(pathPrefix);
        this.resourceManagerSupplier = Objects.requireNonNull(resourceManagerSupplier);
        setSearchUI(new JsonFileSearchUI());
        setCandidateUIProvider(UIElementProvider.text(id -> Component.literal(id.toString())));
        setValue(defaultValue, false);
    }

    @Override
    public void show() {
        super.show();
        onSearchWordChanged("");
    }

    public JsonFileSearchBox setPathPrefix(String pathPrefix) {
        this.pathPrefix = normalizePathPrefix(pathPrefix);
        refreshSearchResults();
        return this;
    }

    @Nullable
    public ResourceLocation getSelectedFileId() {
        return getValue();
    }

    public String getSelectedFileIdString() {
        return getFileIdString(getValue());
    }

    public static String getFileIdString(@Nullable ResourceLocation fileId) {
        return fileId == null ? "" : fileId.toString();
    }

    public static String normalizePathPrefix(String pathPrefix) {
        var normalized = Objects.requireNonNull(pathPrefix).trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith(JSON_SUFFIX)) {
            normalized = normalized.substring(0, normalized.length() - JSON_SUFFIX.length());
        }
        if (!normalized.isEmpty() && !ResourceLocation.isValidPath(normalized)) {
            throw new IllegalArgumentException("Invalid JSON resource file path prefix: " + pathPrefix);
        }
        return normalized;
    }

    private void refreshSearchResults() {
        if (isOpen()) {
            onSearchWordChanged(textField.getValue());
        }
    }

    protected void search(String word, IResultHandler<ResourceLocation> searchHandler) {
        searchJsonFiles(resourceManagerSupplier.get(), pathPrefix, word, searchHandler);
    }

    public static void searchJsonFiles(@Nullable ResourceManager resourceManager,
                                       String pathPrefix,
                                       String word,
                                       IResultHandler<ResourceLocation> searchHandler) {
        if (resourceManager == null) {
            return;
        }

        var normalizedPathPrefix = normalizePathPrefix(pathPrefix);
        var lowerWord = word.toLowerCase(Locale.ROOT);
        resourceManager.listResources(
                        normalizedPathPrefix,
                        location -> isJsonFileUnderPrefix(location, normalizedPathPrefix)
                )
                .keySet()
                .stream()
                .map(JsonFileSearchBox::stripJsonSuffix)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .takeWhile(id -> !Thread.currentThread().isInterrupted())
                .filter(id -> matches(lowerWord, id.toString()))
                .forEach(searchHandler::acceptResult);
    }

    private static boolean isJsonFileUnderPrefix(ResourceLocation location, String pathPrefix) {
        var path = location.getPath();
        return path.endsWith(JSON_SUFFIX) && (pathPrefix.isEmpty() || path.startsWith(pathPrefix));
    }

    private static ResourceLocation stripJsonSuffix(ResourceLocation location) {
        var path = location.getPath();
        return new ResourceLocation(
                location.getNamespace(),
                path.substring(0, path.length() - JSON_SUFFIX.length())
        );
    }

    private static boolean matches(String lowerWord, String text) {
        return lowerWord.isEmpty() || Objects.toString(text, "").toLowerCase(Locale.ROOT).contains(lowerWord);
    }

    private final class JsonFileSearchUI implements ISearchUI<ResourceLocation> {

        @Override
        public String resultText(ResourceLocation value) {
            return value.toString();
        }

        @Override
        public void onResultSelected(@Nullable ResourceLocation value) {
        }

        @Override
        public void search(String word, IResultHandler<ResourceLocation> searchHandler) {
            JsonFileSearchBox.this.search(word, searchHandler);
        }
    }
}
