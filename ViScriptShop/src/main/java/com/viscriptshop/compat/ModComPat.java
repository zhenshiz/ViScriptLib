package com.viscriptshop.compat;

import com.viscriptshop.ViscriptShop;
import net.minecraftforge.api.distmarker.Dist;

public class ModComPat {
    public static void init(Dist dist) {
        if (dist.isClient()) {
            if (ViscriptShop.isFtbLibraryLoaded()) {
                FtbLibraryComPat.init();
            }
        } else {

        }
    }
}
