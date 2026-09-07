package com.viscript_lib.gui.components.search;

import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 提供给定缺失物品 ID 集合的自动补全输入框。
 *
 * <p>候选项在创建时复制并按完整资源位置排序。搜索同时匹配命名空间、路径和完整 ID，
 * 但只有候选集合中的精确 ID 可以作为最终选择。
 */
public class MissingItemIdSearchBox extends SearchComponent<ResourceLocation> {
    private final List<ResourceLocation> candidates;

    /**
     * 创建包含指定缺失物品 ID 的自动补全输入框。
     *
     * @param  candidates 当前项目中存在的缺失物品 ID 集合
     */
    public MissingItemIdSearchBox(Collection<ResourceLocation> candidates) {
        this.candidates = candidates.stream()
                .distinct()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
        setSearchUI(new MissingItemSearchUI());
        setCandidateUIProvider(UIElementProvider.text(id -> Component.literal(id.toString())));
        textField.setResourceLocationOnly();
    }

    /**
     * 返回已选择的 ID，或返回输入框中与候选项完全相同的 ID。
     *
     * <p>任意文本和不属于当前候选集合的资源位置均不会被接受。
     *
     * @return 已选择或精确输入的缺失物品 ID；没有有效选择时返回 <code>null</code>
     */
    @Nullable
    public ResourceLocation getSelectedOrExactId() {
        var selected = getValue();
        if (selected != null) {
            return selected;
        }
        var parsed = ResourceLocation.tryParse(textField.getText());
        return parsed != null && candidates.contains(parsed) ? parsed : null;
    }

    /**
     * 打开候选列表并立即显示全部缺失物品 ID。
     */
    @Override
    public void show() {
        super.show();
        onSearchWordChanged(textField.getText());
    }

    private final class MissingItemSearchUI implements ISearchUI<ResourceLocation> {
        @Override
        public void search(String word, IResultHandler<ResourceLocation> searchHandler) {
            var normalized = word.toLowerCase(Locale.ROOT);
            for (var candidate : candidates) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (normalized.isEmpty()
                        || candidate.toString().toLowerCase(Locale.ROOT).contains(normalized)) {
                    searchHandler.acceptResult(candidate);
                }
            }
        }

        @Override
        public String resultText(ResourceLocation value) {
            return value.toString();
        }

        @Override
        public void onResultSelected(@Nullable ResourceLocation value) {
        }
    }
}
