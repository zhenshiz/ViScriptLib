package com.viscript_recipe.gui.views;

import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import lombok.Getter;
import net.minecraft.network.chat.Component;

public class WorkBenchView extends View {
    private final NavigationView navigationView;
    private final Label titleLabel = RecipeEditorUi.sectionTitle("viscript_recipe.editor.workbench");
    private final Label statusLabel = RecipeEditorUi.label(Component.empty());
    private UIElement canvasStack;
    @Getter
    private RecipeCanvas<?> canvas;

    public WorkBenchView(NavigationView navigationView) {
        super("viscript_recipe.view.workbench", Icons.GRID);
        this.navigationView = navigationView;
        navigationView.addListener(NavigationView.LOAD_CANVAS, this::loadCanvas);
        navigationView.addListener(NavigationView.SAVE_CANVAS, this::saveCanvas);
        navigationView.addListener(NavigationView.UPDATE_STATUS, this::updateStatus);
        addChild(createRoot());
    }

    public RecipeEntry getSelectedEntry() {
        return navigationView.getSelectedEntry();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(8);
            layout.gapAll(8);
            layout.minWidth(0);
            layout.minHeight(0);
        }).style(style -> style.backgroundTexture(Sprites.RECT_SOLID));
        root.setOverflowVisible(false);

        var top = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
            layout.alignItems(AlignItems.CENTER);
            layout.minWidth(0);
        });
        titleLabel.layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
        });
        statusLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.RIGHT)
                .textWrap(TextWrap.HOVER_ROLL)
                .textColor(ColorPattern.LIGHT_GRAY.color));
        statusLabel.layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
            layout.height(18);
        });
        top.addChildren(titleLabel, statusLabel);

        root.addChildren(top, createCanvasStack());
        return root;
    }

    private UIElement createCanvasStack() {
        canvasStack = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.minWidth(0);
            layout.minHeight(0);
//            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        return new RecipeCanvasViewport(canvasStack);
    }

    private void saveCanvas() {
        if (canvas != null) canvas.save();
    }

    private void loadCanvas() {
        if (canvas != null) canvasStack.removeChild(canvas);
        updateStatus();
        var entry = getSelectedEntry();
        if (entry == null) { canvas = null; return; }
        var canvasSupplier = RecipeEditorTypes.require(entry.getType()).canvasSupplier();
        if (canvasSupplier == null) {
            canvas = null;
            titleLabel.setText(Component.literal("The mod for " + entry.getType() + " is not loaded."));
            return;
        }
        canvas = canvasSupplier.apply(navigationView, entry);
        canvas.initVisualState();
        canvas.load();
        canvasStack.addChild(canvas);
    }

    private void updateStatus() {
        var id = navigationView.getSelectedCategoryId();
        titleLabel.setText(RecipeEditorTypes.getCategory(id)
                .map(RecipeEditorCategory::displayName)
                .orElseGet(() -> Component.translatable("viscript_recipe.editor.category.unknown", id)));
        var selectedEntry = getSelectedEntry();
        if (selectedEntry == null) {
            statusLabel.setText(Component.empty());
            return;
        }
        var warningKey = canvas != null && RecipeCanvas.containsUnsupportedIngredients
                ? "viscript_recipe.editor.status.unsupported_ingredient"
                : "viscript_recipe.editor.status.ready";
        statusLabel.setText(Component.translatable(
                "viscript_recipe.editor.status",
                navigationView.getRecipeFile().getEntries().size(),
                navigationView.getRecipeFile().getEntries().stream().filter(RecipeEntry::isEnabled).count(),
                Component.translatable(warningKey)
        ));
    }

    public boolean supportsNotification() {
        RecipeEntry entry = getSelectedEntry();
        return entry != null && entry.getData().getShowNotification() != null;
    }

    public boolean showNotification() {
        return supportsNotification() && getSelectedEntry().getData().getShowNotification();
    }

    public void setShowNotification(boolean value) {
        if (!supportsNotification()) return;
        getSelectedEntry().getData().setShowNotification(value);
    }
}
