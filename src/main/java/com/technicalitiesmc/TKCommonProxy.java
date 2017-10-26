package com.technicalitiesmc;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.io.IOException;
import java.io.InputStream;

public class TKCommonProxy {

    public void bindSpecialRenderers(ASMDataTable asmDataTable) {
    }

    public World getWorld() {
        return null;
    }

    public boolean isGamePaused() {
        return false;
    }

    public InputStream readResource(ResourceLocation path) throws IOException {
        return null;
    }

}
