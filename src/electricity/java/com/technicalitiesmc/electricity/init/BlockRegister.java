package com.technicalitiesmc.electricity.init;

import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.tile.TileTestGenerator;
import com.technicalitiesmc.electricity.tile.TileTestReceiver;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import com.technicalitiesmc.lib.block.BlockTileBaseWithFacing;
import elec332.core.api.registration.IBlockRegister;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class BlockRegister implements IBlockRegister {

	public static Block generator, receiver;

	@Override
	public void register(IForgeRegistry<Block> registry) {
		registry.register(generator = new BlockTileBaseWithFacing(Material.CAKE, TileTestGenerator.class, new TKEResourceLocation("coal_generator")).setCreativeTab(TKElectricity.creativeTab));
		registry.register(receiver = new BlockTileBaseWithFacing(Material.DRAGON_EGG, TileTestReceiver.class, new TKEResourceLocation("receivertest")).setCreativeTab(TKElectricity.creativeTab));
	}

}
