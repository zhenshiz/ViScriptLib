package com.viscript_team.client;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.viscript_team.gui.FactionEditorScreen;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@UtilityClass
public class FactionEditorClientBridge {
    public static void open(CompoundTag snapshot) {
        Minecraft.getInstance().execute(() -> FactionEditorScreen.open(snapshot.copy()));
    }

    public static void sync(CompoundTag snapshot) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof ModularUIScreen screen
                    && screen.modularUI.ui.rootElement instanceof FactionEditorScreen factionEditor) {
                factionEditor.applySnapshot(snapshot.copy());
            }
        });
    }
}
