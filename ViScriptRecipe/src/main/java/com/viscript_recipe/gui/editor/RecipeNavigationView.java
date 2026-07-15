package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.viscript_lib.gui.components.DraggableUI;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RecipeNavigationView extends View {
    private static final int MAX_RECIPE_ID_CANDIDATES = 100;

    private final RecipeEditorController controller;
    private final UIElement entryList = RecipeEditorUi.column();
    private final UIElement addRow = createAddRow();
    private final UIElement importPanel = createImportPanel();
    private WorkstationSearchComponent workstationSearch;
    private List<WorkstationSearchEntry> workstationSearchEntries = List.of();
    private RecipeIdSearchComponent importRecipeSearch;
    private Label importStatusLabel;
    private boolean importOpen;
    private String importRecipeId = "";

    public RecipeNavigationView(RecipeEditorController controller) {
        super("viscript_recipe.view.recipe_navigation", Icons.FILE);
        this.controller = controller;
        addChild(createRoot());
        controller.addListener(this::refresh);
        refresh();
    }

    public void saveCurrentVisualState() {
        controller.saveVisualState();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.panelRoot();
        root.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.entries"),
                RecipeEditorUi.fieldGroup("viscript_recipe.editor.workstation", createWorkstationSearch()),
                addRow,
                importPanel,
                createEntryScroller()
        );
        return root;
    }

    private WorkstationSearchComponent createWorkstationSearch() {
        workstationSearchEntries = createWorkstationSearchEntries();
        workstationSearch = new WorkstationSearchComponent(new SearchComponent.ISearchUI<>() {
            @Override
            public String resultText(RecipeEditorCategory category) {
                return category == null ? "" : category.displayName().getString();
            }

            @Override
            public void onResultSelected(RecipeEditorCategory value) {
            }

            @Override
            public void search(String word, IResultHandler<RecipeEditorCategory> searchHandler) {
                var normalized = normalizeSearch(word);
                for (var entry : workstationSearchEntries) {
                    if (entry.matches(normalized)) {
                        searchHandler.acceptResult(entry.category());
                    }
                }
            }
        });
        workstationSearch.setCandidateUIProvider(this::createWorkstationCandidate)
                .setSelected(controller.getSelectedCategoryData(), false)
                .setOnValueChanged(controller::setSelectedCategory)
                .searchStyle(style -> {
                    style.maxItemCount(8);
                    style.scrollerViewHeight(126);
                });
        workstationSearch.preview.setOverflowVisible(false);
        workstationSearch.textField.setOverflowVisible(false);
        workstationSearch.layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
        });
        return workstationSearch;
    }

    private List<WorkstationSearchEntry> createWorkstationSearchEntries() {
        return controller.availableCategories().stream()
                .map(category -> new WorkstationSearchEntry(category, workstationSearchText(category)))
                .toList();
    }

    private String workstationSearchText(RecipeEditorCategory category) {
        var itemId = category.workstationItemId() == null ? "" : category.workstationItemId().toString();
        return normalizeSearch(String.join(" ",
                category.displayName().getString(),
                Component.translatable(category.translationKey()).getString(),
                category.ownerModId(),
                category.id().toString(),
                itemId
        ));
    }

    private static String normalizeSearch(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }

    private UIElement createWorkstationCandidate(RecipeEditorCategory category) {
        if (category == null) {
            return RecipeEditorUi.label(Component.literal("---")).layout(layout -> {
                layout.widthPercent(100);
                layout.height(14);
            });
        }
        var name = RecipeEditorUi.label(category.displayName())
                .textStyle(style -> style
                        .textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveWidth(false));
        name.setOverflowVisible(false);
        var owner = RecipeEditorUi.label(category.ownerName())
                .textStyle(style -> style
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textColor(ColorPattern.LIGHT_GRAY.color)
                        .textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveWidth(false));
        owner.setOverflowVisible(false);
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(14);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                createWorkstationCell(name),
                createWorkstationCell(owner)
        ).setOverflowVisible(false);
    }

    private UIElement createWorkstationCell(UIElement label) {
        label.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        });
        label.setOverflowVisible(false);
        return new UIElement().layout(layout -> {
            layout.widthPercent(50);
            layout.heightPercent(100);
        }).addChild(label).setOverflowVisible(false);
    }

    private UIElement createAddRow() {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
            layout.gapAll(3);
        }).addChildren(
                RecipeEditorUi.iconButton(Icons.ADD, "viscript_recipe.editor.add_recipe", event -> controller.addEntry())
                        .layout(layout -> layout.flex(1).height(20)),
                RecipeEditorUi.iconButton(Icons.IMPORT, "viscript_recipe.editor.import_recipe", event -> toggleImportPanel())
                        .layout(layout -> layout.flex(1).height(20))
        );
    }

    private UIElement createImportPanel() {
        importRecipeSearch = createImportRecipeSearch();

        var controls = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                importRecipeSearch,
                RecipeEditorUi.iconButton(Icons.CHECK, "viscript_recipe.editor.import_recipe.confirm", event -> submitImport())
                        .layout(layout -> layout.width(20).height(20)),
                RecipeEditorUi.iconButton(Icons.CLOSE, "viscript_recipe.editor.import_recipe.cancel", event -> closeImportPanel())
                        .layout(layout -> layout.width(20).height(20))
        );

        importStatusLabel = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.import_recipe.hint"));
        importStatusLabel.textStyle(style -> style
                .textColor(ColorPattern.LIGHT_GRAY.color)
                .textWrap(TextWrap.HOVER_ROLL)
                .adaptiveWidth(false));
        importStatusLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(14);
        });
        importStatusLabel.setOverflowVisible(false);

        var panel = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(3);
            layout.paddingAll(3);
        }).style(style -> style.backgroundTexture(ColorPattern.T_DARK_GRAY.rectTexture())).addChildren(
                RecipeEditorUi.fieldGroup("viscript_recipe.editor.import_recipe_id", controls),
                importStatusLabel
        );
        panel.setDisplay(false);
        return panel;
    }

    private RecipeIdSearchComponent createImportRecipeSearch() {
        var search = new RecipeIdSearchComponent(new SearchComponent.ISearchUI<>() {
            @Override
            public String resultText(ResourceLocation value) {
                return value == null ? "" : value.toString();
            }

            @Override
            public void onResultSelected(ResourceLocation value) {
                importRecipeId = value == null ? "" : value.toString();
            }

            @Override
            public void search(String word, IResultHandler<ResourceLocation> searchHandler) {
                var minecraft = Minecraft.getInstance();
                if (minecraft.level == null) {
                    return;
                }
                var normalized = normalizeSearch(word);
                var accepted = 0;
                var recipeIds = minecraft.level.getRecipeManager().getRecipeIds()
                        .sorted(Comparator.comparing(ResourceLocation::toString))
                        .toList();
                for (var recipeId : recipeIds) {
                    var holder = minecraft.level.getRecipeManager().byKey(recipeId).orElse(null);
                    if (RecipeImporter.canImport(holder) && matchesRecipeId(recipeId, normalized)) {
                        searchHandler.acceptResult(recipeId);
                        accepted++;
                        if (accepted >= MAX_RECIPE_ID_CANDIDATES) {
                            return;
                        }
                    }
                }
            }
        });
        search.setCandidateUIProvider(this::createRecipeIdCandidate)
                .setOnValueChanged(value -> importRecipeId = value == null ? "" : value.toString())
                .searchStyle(style -> {
                    style.maxItemCount(8);
                    style.scrollerViewHeight(128);
                    style.closeAfterSelect(true);
                });
        search.preview.setOverflowVisible(false);
        search.textField.setOverflowVisible(false);
        search.textField.setResourceLocationOnly();
        search.textField.textFieldStyle(style -> style.placeholder(Component.translatable("viscript_recipe.editor.import_recipe.placeholder")));
        search.layout(layout -> {
            layout.flex(1);
            layout.height(18);
        });
        return search;
    }

    private boolean matchesRecipeId(ResourceLocation recipeId, String normalizedWord) {
        if (normalizedWord.isBlank()) {
            return true;
        }
        var searchText = normalizeSearch(String.join(" ",
                recipeId.toString(),
                recipeId.getNamespace(),
                recipeId.getPath(),
                recipeTypeText(recipeId)
        ));
        for (var token : normalizedWord.split("\\s+")) {
            if (!searchText.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private UIElement createRecipeIdCandidate(ResourceLocation recipeId) {
        if (recipeId == null) {
            return RecipeEditorUi.label(Component.literal("---")).layout(layout -> {
                layout.widthPercent(100);
                layout.height(14);
            });
        }
        var id = RecipeEditorUi.label(Component.literal(recipeId.toString()))
                .textStyle(style -> style
                        .textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveWidth(false));
        id.setOverflowVisible(false);
        return id.layout(layout -> {
            layout.widthPercent(100);
            layout.height(14);
            layout.alignItems(AlignItems.CENTER);
        }).setOverflowVisible(false);
    }

    private String recipeTypeText(ResourceLocation recipeId) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return "";
        }
        return minecraft.level.getRecipeManager()
                .byKey(recipeId)
                .map(holder -> {
                    var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(holder.getType());
                    var serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.getSerializer());
                    if (typeId == null) {
                        return serializerId == null ? "" : serializerId.toString();
                    }
                    if (serializerId == null || serializerId.equals(typeId)) {
                        return typeId.toString();
                    }
                    return typeId + " / " + serializerId;
                })
                .orElse("");
    }

    private void toggleImportPanel() {
        importOpen = !importOpen;
        importPanel.setDisplay(importOpen);
        if (importOpen && (importRecipeId == null || importRecipeId.isBlank())) {
            setImportStatus(Component.translatable("viscript_recipe.editor.import_recipe.hint"), ColorPattern.LIGHT_GRAY.color);
        }
    }

    private void closeImportPanel() {
        importOpen = false;
        importPanel.setDisplay(false);
    }

    private void submitImport() {
        var rawText = importRecipeSearch == null ? "" : importRecipeSearch.textField.getRawText();
        var recipeIdText = rawText.isBlank() ? importRecipeId : rawText.trim();
        var recipeId = ResourceLocation.tryParse(recipeIdText);
        if (recipeId == null) {
            setImportStatus(Component.translatable("viscript_recipe.editor.import_recipe.error.invalid_id"), ColorPattern.RED.color);
            return;
        }
        var result = controller.importRecipe(recipeId);
        setImportStatus(result.message(), result.successful() ? ColorPattern.GREEN.color : ColorPattern.RED.color);
        if (result.successful()) {
            importRecipeId = "";
            importRecipeSearch.setSelected(null, false);
            importRecipeSearch.textField.setText("", false);
        }
    }

    private void setImportStatus(Component message, int color) {
        if (importStatusLabel == null) {
            return;
        }
        importStatusLabel.setText(message);
        importStatusLabel.textStyle(style -> style
                .textColor(color)
                .textWrap(TextWrap.HOVER_ROLL)
                .adaptiveWidth(false));
    }

    private ScrollerView createEntryScroller() {
        var scroller = new ScrollerView();
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        scroller.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        scroller.viewPort.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        scroller.viewPort.layout(layout -> layout.paddingAll(0));
        scroller.verticalScroller.headButton.setDisplay(false);
        scroller.verticalScroller.tailButton.setDisplay(false);
        scroller.horizontalScroller.setDisplay(false);
        scroller.addScrollViewChild(entryList.layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(2);
        }));
        return scroller;
    }

    private void refresh() {
        refreshWorkstationSearch();
        entryList.clearAllChildren();
        var entries = controller.recipeFile().getEntries();
        var visibleEntries = entries.stream()
                .filter(controller::isEntryInSelectedCategory)
                .toList();
        var draggableEntries = new DraggableUI<>(visibleEntries, controller::reorderSelectedCategoryEntries);
        draggableEntries.layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(2);
            layout.paddingAll(0);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.wrap(FlexWrap.NO_WRAP);
        });
        var visibleIndex = 0;
        for (var entry : visibleEntries) {
            var dragHandle = createDragHandle();
            draggableEntries.addSortableCard(entry, createEntryRow(entry, visibleIndex++, dragHandle), dragHandle);
        }
        draggableEntries.addChild(createTailDropZone());
        entryList.addChild(draggableEntries);
    }

    private void refreshWorkstationSearch() {
        workstationSearchEntries = createWorkstationSearchEntries();
        if (workstationSearch == null || workstationSearch.isOpen()) {
            return;
        }
        var selectedCategory = controller.getSelectedCategoryData();
        if (workstationSearch.getValue() != selectedCategory) {
            workstationSearch.setSelected(selectedCategory, false);
        }
    }

    private UIElement createEntryRow(RecipeEntry entry, int index, UIElement dragHandle) {
        var selected = entry == controller.getSelectedEntry();
        var label = entryLabel(entry, index);
        var button = new Button()
                .setText(label)
                .setOnClick(event -> controller.selectEntry(entry))
                .textStyle(style -> style
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveWidth(false)
                        .textColor(entryTextColor(entry)));
        button.text.layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
        });
        button.text.setOverflowVisible(false);
        button.layout(layout -> {
            layout.widthPercent(100);
            layout.height(22);
        });
        button.setOverflowVisible(false);
        button.buttonStyle(style -> style
                .baseTexture(selected ? ColorPattern.GRAY.rectTexture() : ColorPattern.DARK_GRAY.rectTexture())
                .hoverTexture(ColorPattern.GRAY.rectTexture())
                .pressedTexture(ColorPattern.SEAL_BLACK.rectTexture()));
        return new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.height(22);
            layout.gapAll(2);
            layout.flexDirection(FlexDirection.ROW);
        }).addChildren(
                dragHandle,
                button.layout(layout -> layout.flex(1)),
                RecipeEditorUi.iconButton(Icons.COPY, "viscript_recipe.editor.duplicate", event -> controller.duplicateEntry(entry))
                        .layout(layout -> layout.width(20).height(22)),
                RecipeEditorUi.iconButton(Icons.DELETE, "viscript_recipe.editor.delete", event -> controller.removeEntry(entry))
                        .layout(layout -> layout.width(20).height(22))
        );
    }

    private UIElement createDragHandle() {
        var handle = RecipeEditorUi.iconButton(Icons.GRID, "viscript_recipe.editor.drag_reorder", event -> {
        });
        handle.layout(layout -> {
            layout.width(18);
            layout.height(22);
        });
        handle.buttonStyle(style -> style
                .baseTexture(ColorPattern.DARK_GRAY.rectTexture())
                .hoverTexture(ColorPattern.GRAY.rectTexture())
                .pressedTexture(ColorPattern.SEAL_BLACK.rectTexture()));
        return handle;
    }

    private UIElement createTailDropZone() {
        return new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
    }

    private int entryTextColor(RecipeEntry entry) {
        if (!entry.isEnabled()) {
            return ColorPattern.LIGHT_GRAY.color;
        }
        return ColorPattern.WHITE.color;
    }

    private Component entryLabel(RecipeEntry entry, int index) {
        var id = entry.getRecipeId() == null ? "?" : entry.getRecipeId().toString();
        return Component.translatable(
                "viscript_recipe.editor.entry_label",
                index + 1,
                controller.typeDisplayName(entry),
                id
        );
    }

    private record WorkstationSearchEntry(RecipeEditorCategory category, String searchText) {
        private boolean matches(String normalizedWord) {
            if (normalizedWord.isBlank()) {
                return true;
            }
            for (var token : normalizedWord.split("\\s+")) {
                if (!searchText.contains(token)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class WorkstationSearchComponent extends SearchComponent<RecipeEditorCategory> {
        private WorkstationSearchComponent(ISearchUI<RecipeEditorCategory> searchUI) {
            super(searchUI);
        }

        @Override
        public void show() {
            super.show();
            textField.setText("", false);
            onSearchWordChanged("");
        }
    }

    private class RecipeIdSearchComponent extends SearchComponent<ResourceLocation> {
        private RecipeIdSearchComponent(ISearchUI<ResourceLocation> searchUI) {
            super(searchUI);
        }

        @Override
        protected void onSearchWordChanged(String word) {
            importRecipeId = word == null ? "" : word;
            super.onSearchWordChanged(word);
        }

        @Override
        public void show() {
            super.show();
            textField.setText(importRecipeId == null ? "" : importRecipeId, false);
            onSearchWordChanged(importRecipeId == null ? "" : importRecipeId);
        }
    }
}
