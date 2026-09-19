package com.viscript_recipe.compat.mekanism.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.mekanism.MekanismRecipeFactory;
import com.viscript_recipe.compat.mekanism.data.MekanismChemicalIngredientData;
import com.viscript_recipe.compat.mekanism.data.MekanismChemicalIngredientKind;
import com.viscript_recipe.compat.mekanism.data.MekanismChemicalStackData;
import dev.vfyjxf.taffy.style.TaffyPosition;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.client.gui.GuiUtils;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Renders a Mekanism chemical as a tinted gauge or compact bar for recipe previews.
 */
class ChemicalDisplay {
    MekanismChemicalIngredientData input = MekanismChemicalIngredientData.empty();
    MekanismChemicalStackData output = MekanismChemicalStackData.empty();

    private final ChemicalTexture fillTexture = new ChemicalTexture();
    private final UIElement fill;
    private final UIElement root;

    ChemicalDisplay(String gaugeFrame) {
        this(gaugeFrame, "standard.png", 16, 58);
    }

    ChemicalDisplay(String gaugeFrame, String gaugeOverlay, int overlayWidth, int overlayHeight) {
        this(overlayWidth, overlayHeight,
                SpriteTexture.of(new ResourceLocation(
                        "mekanism", "gui/gauge/" + gaugeFrame
                )).setSprite(0, 0, 5, 5).setBorder(2),
                SpriteTexture.of(new ResourceLocation(
                        "mekanism", "gui/gauge/" + gaugeOverlay
                )).setSprite(0, 0, overlayWidth, overlayHeight)
        );
    }

    ChemicalDisplay(int barWidth, int barHeight) {
        this(barWidth, barHeight,
                SpriteTexture.of(new ResourceLocation(
                        "mekanism", "gui/bar/base.png"
                )).setSprite(0, 0, 5, 5).setBorder(2),
                IGuiTexture.EMPTY
        );
    }

    private ChemicalDisplay(int contentWidth, int contentHeight, IGuiTexture frameTexture, IGuiTexture overlayTexture) {
        fill = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(contentWidth);
            layout.height(contentHeight);
        }).style(style -> style.backgroundTexture(fillTexture));
        var overlay = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(contentWidth);
            layout.height(contentHeight);
        }).style(style -> style.backgroundTexture(overlayTexture));
        root = new UIElement().layout(layout -> {
            layout.width(contentWidth + 2);
            layout.height(contentHeight + 2);
        }).style(style -> style.backgroundTexture(frameTexture));
        root.addChildren(fill, overlay);
    }

    static ItemStack[] catalystStacks(MekanismChemicalIngredientData data) {
        if (data == null) return new ItemStack[0];
        try {
            var ingredient = MekanismRecipeFactory.chemicalIngredient(data, "ingredient");
            return switch (data.getChemicalType()) {
                case GAS -> MekanismJEI.GAS_STACK_HELPER.getStacksFor((Gas) ingredient.getRepresentations().get(0).getRaw(), true).toArray(new ItemStack[0]);
                case INFUSION -> MekanismJEI.INFUSION_STACK_HELPER.getStacksFor((InfuseType) ingredient.getRepresentations().get(0).getRaw(), true).toArray(new ItemStack[0]);
                case PIGMENT -> MekanismJEI.PIGMENT_STACK_HELPER.getStacksFor((Pigment) ingredient.getRepresentations().get(0).getRaw(), true).toArray(new ItemStack[0]);
                case SLURRY -> MekanismJEI.SLURRY_STACK_HELPER.getStacksFor((Slurry) ingredient.getRepresentations().get(0).getRaw(), true).toArray(new ItemStack[0]);
            };
        } catch (Exception e) {
            return new ItemStack[0];
        }
    }

    static Chemical<?> getFirstChemical(ChemicalType type, ResourceLocation tagId) {
        if (type == null || tagId == null) return null;
        return switch (type) {
            case GAS -> {
                var tag = ChemicalTags.GAS.tag(tagId);
                yield MekanismAPI.gasRegistry().tags().getTag(tag).stream().findFirst().orElse(null);
            }
            case INFUSION -> {
                var tag = ChemicalTags.INFUSE_TYPE.tag(tagId);
                yield MekanismAPI.infuseTypeRegistry().tags().getTag(tag).stream().findFirst().orElse(null);
            }
            case PIGMENT -> {
                var tag = ChemicalTags.PIGMENT.tag(tagId);
                yield MekanismAPI.pigmentRegistry().tags().getTag(tag).stream().findFirst().orElse(null);
            }
            case SLURRY -> {
                var tag = ChemicalTags.SLURRY.tag(tagId);
                yield MekanismAPI.slurryRegistry().tags().getTag(tag).stream().findFirst().orElse(null);
            }
        };
    }

    static int colorRepresentation(MekanismChemicalIngredientData data) {
        if (data == null) return 0xFFFFFFFF;
        Chemical<?> chemical;
        ChemicalType type = data.getChemicalType();
        if (data.getKind() == MekanismChemicalIngredientKind.TAG) {
            chemical = getFirstChemical(type, data.getTag());
        } else {
            chemical = chemical(type, data.getChemical());
        }
        return colorRepresentation(chemical);
    }

    static int colorRepresentation(MekanismChemicalStackData data) {
        return colorRepresentation(data == null ? null : chemical(data.getChemicalType(), data.getChemical()));
    }

    public UIElement element() {return root;}

    public void setInput(MekanismChemicalIngredientData data) {
        if (data == null || data.isEmpty()) {
            set(null, Component.empty());
            return;
        }
        input = data;
        var amount = Math.max(1, data.getAmount());
        if (data.getKind() == MekanismChemicalIngredientKind.TAG) {
            var chemical = getFirstChemical(data.getChemicalType(), data.getTag());
            set(chemical, Component.literal("#" + data.getTag() + " × " + amount));
            return;
        }
        set(chemical(data.getChemicalType(), data.getChemical()), Component.literal(data.getChemical() + " × " + amount));
    }

    public void setOutput(MekanismChemicalStackData data) {
        if (data == null || data.isEmpty()) {
            set(null, Component.empty());
            return;
        }
        output = data;
        set(chemical(data.getChemicalType(), data.getChemical()), Component.literal(data.getChemical() + " × " + data.getAmount()));
    }

    private void set(Chemical<?> chemical, Component fallback) {
        if (chemical == null || chemical == ChemicalUtil.getEmptyStack(ChemicalType.getTypeFor(chemical)).getRaw()) {
            fill.setDisplay(false);
            root.style(style -> style.tooltips(fallback));
            return;
        }
        fill.setDisplay(true);
        fillTexture.setChemical(chemical);
        root.style(style -> style.tooltips(chemical.getTextComponent().copy()
                .append(Component.literal(" × " + amountFromFallback(fallback)))));
    }

    private static Chemical<?> chemical(ChemicalType type, ResourceLocation id) {
        if (type == null || id == null) return null;
        return switch (type) {
            case GAS -> MekanismAPI.gasRegistry().getValue(id);
            case INFUSION -> MekanismAPI.infuseTypeRegistry().getValue(id);
            case PIGMENT -> MekanismAPI.pigmentRegistry().getValue(id);
            case SLURRY -> MekanismAPI.slurryRegistry().getValue(id);
        };
    }

    private static int colorRepresentation(Chemical<?> chemical) {
        if (chemical == null) {
            return 0xFFFFFFFF;
        }
        int color = chemical.getColorRepresentation();
        return (color & 0xFF000000) == 0 ? 0xFF000000 | color : color;
    }

    private static String amountFromFallback(Component fallback) {
        var text = fallback.getString();
        var marker = text.lastIndexOf('×');
        return marker < 0 ? "" : text.substring(marker + 1).trim();
    }

    static final class ChemicalTexture implements IGuiTexture {
        private Chemical<?> chemical;

        private void setChemical(Chemical<?> chemical) {this.chemical = chemical;}

        @Override
        public void draw(
                GuiGraphics graphics,
                float mouseX,
                float mouseY,
                float x,
                float y,
                float width,
                float height,
                float partialTicks
        ) {
            if (chemical == null || width <= 0 || height <= 0) {
                return;
            }
            graphics.flush();
            MekanismRenderer.color(graphics, 0xFF000000 | chemical.getTint());
            GuiUtils.drawTiledSprite(
                    graphics,
                    Math.round(x),
                    Math.round(y),
                    Math.round(height),
                    Math.round(width),
                    Math.round(height),
                    MekanismRenderer.getSprite(chemical.getIcon()),
                    16,
                    16,
                    100,
                    GuiUtils.TilingDirection.UP_RIGHT
            );
            MekanismRenderer.resetColor(graphics);
        }
    }

    static final class EnergyTexture implements IGuiTexture {
        @Override
        public void draw(
                GuiGraphics graphics,
                float mouseX,
                float mouseY,
                float x,
                float y,
                float width,
                float height,
                float partialTicks
        ) {
            if (MekanismRenderer.energyIcon == null || width <= 0 || height <= 0) {
                return;
            }
            graphics.flush();
            GuiUtils.drawTiledSprite(
                    graphics,
                    Math.round(x),
                    Math.round(y),
                    Math.round(height),
                    Math.round(width),
                    Math.round(height),
                    MekanismRenderer.energyIcon,
                    16,
                    16,
                    100,
                    GuiUtils.TilingDirection.UP_RIGHT
            );
        }
    }
}
