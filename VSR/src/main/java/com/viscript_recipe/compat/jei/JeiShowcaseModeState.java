package com.viscript_recipe.compat.jei;

import com.viscript_recipe.ViScriptRecipe;
import lombok.Getter;

/**
 * Keeps the server-synced showcase mode flag without depending on JEI classes.
 */
public final class JeiShowcaseModeState {
    private static final Runnable NOOP = () -> {
    };

    @Getter
    private static volatile boolean showcaseOnly;
    private static volatile boolean syncedFromServer;
    private static volatile Runnable runtimeApplier = NOOP;

    private JeiShowcaseModeState() {
    }

    public static void updateFromLocalConfig(boolean showcaseOnly) {
        if (!syncedFromServer) {
            setShowcaseOnly(showcaseOnly);
            return;
        }
        applyCurrentState();
    }

    public static void updateFromServer(boolean showcaseOnly) {
        syncedFromServer = true;
        setShowcaseOnly(showcaseOnly);
    }

    public static void setRuntimeApplier(Runnable applier) {
        runtimeApplier = applier == null ? NOOP : applier;
        applyCurrentState();
    }

    public static void clearRuntimeApplier() {
        runtimeApplier = NOOP;
    }

    public static void applyCurrentState() {
        try {
            runtimeApplier.run();
        } catch (RuntimeException | LinkageError e) {
            ViScriptRecipe.LOGGER.warn("Failed to apply JEI showcase mode", e);
        }
    }

    private static void setShowcaseOnly(boolean showcaseOnly) {
        JeiShowcaseModeState.showcaseOnly = showcaseOnly;
        applyCurrentState();
    }
}
