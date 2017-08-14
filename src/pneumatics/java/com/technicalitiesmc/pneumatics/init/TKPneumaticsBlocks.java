package com.technicalitiesmc.pneumatics.init;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.TKBase;
import com.technicalitiesmc.lib.item.ItemBlockBase;
import com.technicalitiesmc.pneumatics.block.BlockPneumaticTube;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeClient;
import com.technicalitiesmc.pneumatics.tile.TilePneumaticTubeServer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TKPneumaticsBlocks {

    public static Block pneumatic_tube;

    public static void initialize() {
        pneumatic_tube = new BlockPneumaticTube();
    }

    public static void register() {
        Technicalities.register(pneumatic_tube.setRegistryName("pneumatic_tube"));
        Technicalities.register(new ItemBlockBase(pneumatic_tube)).accept(() -> TKBase.proxy.registerItemModel(pneumatic_tube, 0,
                new ModelResourceLocation(pneumatic_tube.getRegistryName(), "inventory")));
        GameRegistry.registerTileEntity(TilePneumaticTubeServer.class, "pneumatic_tube");
        GameRegistry.registerTileEntity(TilePneumaticTubeClient.class, "pneumatic_tube.client");
    }

}
