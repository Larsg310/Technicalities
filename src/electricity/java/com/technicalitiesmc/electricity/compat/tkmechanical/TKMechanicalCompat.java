package com.technicalitiesmc.electricity.compat.tkmechanical;

import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import com.technicalitiesmc.lib.block.BlockTileBaseWithFacing;
import elec332.core.api.module.ElecModule;
import elec332.core.util.RegistryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by Elec332 on 23-11-2017.
 */
@ElecModule(owner = TKElectricity.MODID, name = "Technicalities-Mechanical Compat", modDependencies = "tkmechanical")
public class TKMechanicalCompat {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Block engine = new BlockTileBaseWithFacing(Material.CACTUS, TileElectricalEngine.class, new TKEResourceLocation("electrical_engine"));
		engine.setCreativeTab(TKElectricity.creativeTab);
		RegistryHelper.register(engine);
		RegistryHelper.register(new ItemBlock(engine).setRegistryName(engine.getRegistryName()));
	}

}
