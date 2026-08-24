package com.viscript_recipe.data;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public interface ITranslated extends StringRepresentable {

    String name();

    default @NotNull String getSerializedName() {return name().toLowerCase();}

    String translatePrefix();

    default Component displayName() {return Component.translatable(translatePrefix() + getSerializedName());}
}
