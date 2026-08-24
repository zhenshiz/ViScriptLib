package com.viscript_recipe.gui.views;

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
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NavigationView extends View {
    private static final int MAX_RECIPE_ID_CANDIDATES = 100;

    private final RecipeProject project;
    @Getter @Nullable
    private RecipeEntry selectedEntry;
    @Getter
    private ResourceLocation selectedCategoryId = RecipeEditorTypes.CRAFTING_TABLE;
    @Getter
    private SlotSelection slotSelection = SlotSelection.RECIPE;
    private final UIElement entryList = RecipeEditorUi.column();
    private final UIElement addRow = createAddRow();
    private final UIElement importPanel = createImportPanel();
    private final UIElement showAllEntriesButton = createShowAllEntriesButton();
    private WorkstationSearchComponent workstationSearch;
    private List<WorkstationSearchEntry> workstationSearchEntries = List.of();
    private RecipeIdSearchComponent importRecipeSearch;
    private Label importStatusLabel;
    private boolean importOpen;
    private String importRecipeId = "";
    private static boolean showAllEntries = false;

    public static final String SLOT_SELECTION_CHANGED = "slot_selection_changed";
    public static final String LOAD_CANVAS = "load_canvas";
    public static final String SAVE_CANVAS = "save_canvas";
    public static final String UPDATE_STATUS = "update_status";
    private final HashMap<String, Runnable> listeners = new HashMap<>();

    public NavigationView(RecipeProject project) {
        super("viscript_recipe.view.recipe_navigation", Icons.FILE);
        this.project = project;
        addChild(createRoot());
        refresh();
    }

    public void addListener(String id, Runnable listener) {listeners.put(id, listener);}

    public void setSlotSelection(SlotSelection selection) {
        slotSelection = selection;
        refreshPropertiesView();
    }
    public void selectRecipe() {setSlotSelection(SlotSelection.RECIPE);}
    public void refreshPropertiesView() {listeners.get(SLOT_SELECTION_CHANGED).run();}
    public void loadCanvas() {
        listeners.get(LOAD_CANVAS).run();
        refresh();
    }
    public void saveCanvas() {listeners.get(SAVE_CANVAS).run();}
    public void reloadCanvas() {saveCanvas(); loadCanvas();}
    public void updateStatus() {listeners.get(UPDATE_STATUS).run();}

    private UIElement createRoot() {
        var root = RecipeEditorUi.panelRoot();
        root.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.entries"),
                RecipeEditorUi.fieldGroup("viscript_recipe.editor.workstation", createWorkstationSearch()),
                addRow,
                importPanel,
                showAllEntriesButton,
                createEntryScroller()
        );
        return root;
    }

    private WorkstationSearchComponent createWorkstationSearch() {
        workstationSearchEntries = createWorkstationSearchEntries();
        workstationSearch = new WorkstationSearchComponent(new SearchComponent.ISearchUI<>() {
            @Override
            public @NotNull String resultText(@NotNull RecipeEditorCategory category) {
                return category.displayName().getString();
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
                .setSelected(getSelectedCategory(), false)
                .setOnValueChanged(this::setSelectedCategoryId)
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

    public RecipeFile getRecipeFile() {return project.getRecipeFile();}

    public RecipeEditorCategory getSelectedCategory() {
        return RecipeEditorTypes.requireCategory(selectedCategoryId);
    }

    public void addEntry() {
        var type = RecipeEditorTypes.defaultTypeForCategory(selectedCategoryId);
        var entry = new RecipeEntry().setType(type).setRecipeId(nextGeneratedRecipeId());
        entry.applyDefaultData();
        getRecipeFile().getEntries().add(entry);
        selectEntry(entry);
    }

    public void duplicateEntry(RecipeEntry entry) {
        if (entry == null) return;
        var entries = getRecipeFile().getEntries();
        var sourceIndex = entries.indexOf(entry);
        if (sourceIndex < 0) {
            return;
        }
        var duplicate = entry.copy().setRecipeId(nextDuplicateRecipeId(entry.getRecipeId()));
        entries.add(sourceIndex + 1, duplicate);
        selectEntry(duplicate);
    }

    public RecipeImportResult importRecipe(ResourceLocation recipeId) {
        if (recipeId == null) {
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.invalid_id");
        }
        if (recipeIdExists(recipeId)) {
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.duplicate_id");
        }
        var result = RecipeImporter.importRecipe(recipeId);
        if (!result.successful() || result.entry() == null) {
            return result;
        }
        var entry = result.entry();
        var entries = getRecipeFile().getEntries();
        var insertIndex = selectedEntry == null ? entries.size() : entries.indexOf(selectedEntry) + 1;
        if (insertIndex <= 0 || insertIndex > entries.size()) {
            insertIndex = entries.size();
        }
        entries.add(insertIndex, entry);
        selectEntry(entry);
        return result;
    }

    public void removeEntry(RecipeEntry entry) {
        var entries = getRecipeFile().getEntries();
        var removedIndex = entries.indexOf(entry);
        entries.remove(entry);
        selectEntry(findNearestEntryInCategory(removedIndex, selectedCategoryId));
    }

    public void selectEntry(RecipeEntry entry) {
        if (entry != null) {
            var category = RecipeEditorTypes.require(entry.getType()).category();
            if (!category.equals(selectedCategoryId)) selectedCategoryId = category;
        }
        saveCanvas();
        selectedEntry = entry;
        loadCanvas();
        selectRecipe();
        refresh();
    }

    public RecipeEditorType getSelectedRecipeType() {
        return selectedEntry == null ? null : RecipeEditorTypes.require(selectedEntry.getType());
    }

    public void setSelectedRecipeType(RecipeEditorType type) {
        if (selectedEntry == null || !type.isAvailable() || !type.category().equals(selectedCategoryId)) {
            return;
        }
        saveCanvas();
        selectedEntry.setType(type.id()).applyDefaultData();
        loadCanvas();
        selectRecipe();
    }

    public void reorderSelectedCategoryEntries(List<RecipeEntry> orderedEntries) {
        if (orderedEntries == null || orderedEntries.isEmpty()) return;
        var entries = getRecipeFile().getEntries();
        var visibleEntries = entries.stream().filter(this::shouldShow).toList();
        if (visibleEntries.size() != orderedEntries.size() || !containsSameEntries(visibleEntries, orderedEntries)) {
            return;
        }
        if (sameEntryOrder(visibleEntries, orderedEntries)) return;
        var nextIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (shouldShow(entries.get(i))) entries.set(i, orderedEntries.get(nextIndex++));
        }
    }

    @Nullable
    private RecipeEntry findNearestEntryInCategory(int preferredIndex, ResourceLocation category) {
        var entries = getRecipeFile().getEntries();
        if (entries.isEmpty()) {
            return null;
        }
        for (int i = Math.min(preferredIndex, entries.size() - 1); i >= 0; i--) {
            var entry = entries.get(i);
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        for (int i = Math.max(0, preferredIndex); i < entries.size(); i++) {
            var entry = entries.get(i);
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        return null;
    }

    private ResourceLocation nextDuplicateRecipeId(@Nullable ResourceLocation sourceId) {
        var sourcePath = sourceId == null ? "recipe" : sourceId.getPath();
        var namespace = getRecipeFile().getRecipeNamespace();
        var copyPath = sourcePath + "_copy";
        var candidate = new ResourceLocation(namespace, copyPath);
        if (!recipeIdExists(candidate)) {
            return candidate;
        }
        var index = 2;
        while (true) {
            candidate = new ResourceLocation(namespace, copyPath + "_" + index);
            if (!recipeIdExists(candidate)) {
                return candidate;
            }
            index++;
        }
    }

    private ResourceLocation nextGeneratedRecipeId() {
        var namespace = getRecipeFile().getRecipeNamespace();
        var index = getRecipeFile().getEntries().size() + 1;
        while (true) {
            var candidate = new ResourceLocation(namespace, "recipe_" + index);
            if (!recipeIdExists(candidate)) {
                return candidate;
            }
            index++;
        }
    }

    private boolean recipeIdExists(ResourceLocation recipeId) {
        for (var entry : getRecipeFile().getEntries()) {
            if (recipeId.equals(entry.getRecipeId())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSameEntries(List<RecipeEntry> currentEntries, List<RecipeEntry> orderedEntries) {
        var remaining = Collections.newSetFromMap(new IdentityHashMap<RecipeEntry, Boolean>());
        remaining.addAll(currentEntries);
        for (var entry : orderedEntries) {
            if (!remaining.remove(entry)) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    private boolean sameEntryOrder(List<RecipeEntry> currentEntries, List<RecipeEntry> orderedEntries) {
        for (int i = 0; i < currentEntries.size(); i++) {
            if (currentEntries.get(i) != orderedEntries.get(i)) {
                return false;
            }
        }
        return true;
    }

    public void setSelectedCategoryId(RecipeEditorCategory category) {
        if (category == null || !category.isAvailable() || selectedCategoryId.equals(category.id())) {
            return;
        }
        selectedCategoryId = category.id();
        saveCanvas();
        selectedEntry = findFirstEntryInCategory(selectedCategoryId);
        loadCanvas();
        selectRecipe();
    }

    @Nullable
    private RecipeEntry findFirstEntryInCategory(ResourceLocation category) {
        for (var entry : getRecipeFile().getEntries()) {
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        return null;
    }

    static boolean isEntryInCategory(RecipeEntry entry, ResourceLocation category) {
        return RecipeEditorTypes.isInCategory(entry.getType(), category);
    }

    private List<WorkstationSearchEntry> createWorkstationSearchEntries() {
        return RecipeEditorTypes.availableCategories().stream()
                .map(WorkstationSearchEntry::new).toList();
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
                RecipeEditorUi.iconButton(Icons.ADD, "viscript_recipe.editor.add_recipe", event -> addEntry())
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
            public @NotNull String resultText(@NotNull ResourceLocation value) {return value.toString();}

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
                        if (accepted >= MAX_RECIPE_ID_CANDIDATES) return;
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
        var result = importRecipe(recipeId);
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

    private UIElement createShowAllEntriesButton() {
        return RecipeCanvas.switchField("viscript_recipe.editor.show_all_recipes",
                showAllEntries, bl -> { showAllEntries = bl; refresh(); });
    }

    private void refresh() {
        refreshWorkstationSearch();
        entryList.clearAllChildren();
        var visibleEntries = getRecipeFile().getEntries().stream().filter(this::shouldShow).toList();
        var draggableEntries = new DraggableUI<>(visibleEntries, this::reorderSelectedCategoryEntries);
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

    private boolean shouldShow(RecipeEntry entry) {
        return showAllEntries ? RecipeEditorTypes.get(entry.getType()).isPresent() : isEntryInCategory(entry, selectedCategoryId);
    }

    private void refreshWorkstationSearch() {
        workstationSearchEntries = createWorkstationSearchEntries();
        if (workstationSearch == null || workstationSearch.isOpen()) {
            return;
        }
        var selectedCategory = getSelectedCategory();
        if (workstationSearch.getValue() != selectedCategory) {
            workstationSearch.setSelected(selectedCategory, false);
        }
    }

    private UIElement createEntryRow(RecipeEntry entry, int index, UIElement dragHandle) {
        var selected = entry == selectedEntry;
        var label = entryLabel(entry, index);
        var button = new Button()
                .setText(label)
                .setOnClick(event -> selectEntry(entry))
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
                RecipeEditorUi.iconButton(Icons.COPY, "viscript_recipe.editor.duplicate", event -> duplicateEntry(entry))
                        .layout(layout -> layout.width(20).height(22)),
                RecipeEditorUi.iconButton(Icons.DELETE, "viscript_recipe.editor.delete", event -> removeEntry(entry))
                        .layout(layout -> layout.width(20).height(22))
        );
    }

    static UIElement createDragHandle() {
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

    static UIElement createTailDropZone() {
        return new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
    }

    static int entryTextColor(RecipeEntry entry) {
        return entry.isEnabled() ? ColorPattern.WHITE.color : ColorPattern.LIGHT_GRAY.color;
    }

    static Component entryLabel(RecipeEntry entry, int index) {
        return Component.translatable(
                "viscript_recipe.editor.entry_label",
                index + 1,
                RecipeEditorTypes.require(entry.getType()).displayName(),
                entry.getRecipeId().toString()
        );
    }

    private record WorkstationSearchEntry(RecipeEditorCategory category, String searchText) {
        WorkstationSearchEntry(RecipeEditorCategory category) {
            this(category, workstationSearchText(category));
        }

        private boolean matches(String normalizedWord) {
            if (normalizedWord.isBlank()) return true;
            for (var token : normalizedWord.split("\\s+")) {
                if (!searchText.contains(token)) {
                    return false;
                }
            }
            return true;
        }

        static String workstationSearchText(RecipeEditorCategory category) {
            return normalizeSearch(String.join(" ",
                    category.displayName().getString(),
                    Component.translatable(category.translationKey()).getString(),
                    category.ownerModId(),
                    category.id().toString(),
                    category.workstationItemId() == null ? "" : category.workstationItemId().toString()
            ));
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
        protected void onSearchWordChanged(@NotNull String word) {
            importRecipeId = word;
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
