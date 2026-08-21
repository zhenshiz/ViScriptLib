package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 连接自定义注册表补全实现和 LDLib2 {@link SearchComponent} 的桥梁基类。
 *
 * <p>具体补全逻辑应该写在各自的子类中，例如 {@link ItemSearchBox}、
 * {@link BlockSearchBox}、{@link DimensionSearchBox}。
 */
public abstract class RegistrySearchBox<T> extends SearchComponent<T> {
    private final Supplier<Registry<?>> valueRegistry;
    private final Function<T, ResourceLocation> idGetter;
    private Predicate<? super T> candidateFilter = value -> true;

    protected RegistrySearchBox(@Nullable T defaultValue,
                                Supplier<Registry<?>> valueRegistry,
                                Function<T, ResourceLocation> idGetter,
                                Function<T, String> resultText,
                                SearchAction<T> searchAction,
                                UIElementProvider<T> candidateUIProvider) {
        super();
        this.valueRegistry = valueRegistry;
        this.idGetter = idGetter;
        setSearchUI(new BridgedSearchUI<>(resultText, searchAction, this::matchesCandidateFilter));
        setCandidateUIProvider(candidateUIProvider);
        setValue(defaultValue, false);
    }

    @Override
    public void show() {
        super.show();
        onSearchWordChanged("");
    }

    @Nullable
    public ResourceLocation getSelectedId() {
        return getId(getValue());
    }

    public String getSelectedIdString() {
        return getIdString(getValue());
    }

    @Nullable
    public ResourceLocation getId(@Nullable T value) {
        return value == null ? null : idGetter.apply(value);
    }

    public String getIdString(@Nullable T value) {
        return idString(getId(value));
    }

    @Nullable
    public Registry<?> getValueRegistry() {
        return valueRegistry.get();
    }

    public RegistrySearchBox<T> setCandidateFilter(Predicate<? super T> candidateFilter) {
        this.candidateFilter = Objects.requireNonNull(candidateFilter);
        refreshSearchResults();
        return this;
    }

    public RegistrySearchBox<T> clearCandidateFilter() {
        return setCandidateFilter(value -> true);
    }

    protected boolean matchesCandidateFilter(T value) {
        return candidateFilter.test(value);
    }

    protected void refreshSearchResults() {
        if (isOpen()) {
            onSearchWordChanged(textField.getValue());
        }
    }

    protected static <V> void searchRegistry(Registry<V> registry,
                                             String word,
                                             IResultHandler<V> searchHandler,
                                             Function<V, String> extraSearchText) {
        var lowerWord = word.toLowerCase(Locale.ROOT);
        for (var key : registry.keySet()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            V value = registry.get(key);
            if (matches(lowerWord, key.toString()) || matches(lowerWord, extraSearchText.apply(value))) {
                searchHandler.acceptResult(value);
            }
        }
    }

    protected static boolean matches(String lowerWord, String text) {
        return lowerWord.isEmpty() || Objects.toString(text, "").toLowerCase(Locale.ROOT).contains(lowerWord);
    }

    protected static String idString(@Nullable ResourceLocation id) {
        return id == null ? "" : id.toString();
    }

    @FunctionalInterface
    protected interface SearchAction<T> {
        void search(String word, IResultHandler<T> searchHandler);
    }

    private record BridgedSearchUI<T>(
            Function<T, String> resultText,
            SearchAction<T> searchAction,
            Predicate<? super T> candidateFilter
    ) implements ISearchUI<T> {

        @Override
        public String resultText(T value) {
            return resultText.apply(value);
        }

        @Override
        public void onResultSelected(@Nullable T value) {
        }

        @Override
        public void search(String word, IResultHandler<T> searchHandler) {
            searchAction.search(word, value -> {
                if (candidateFilter.test(value)) {
                    searchHandler.acceptResult(value);
                }
            });
        }
    }
}
