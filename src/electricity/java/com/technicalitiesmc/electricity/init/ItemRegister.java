package com.technicalitiesmc.electricity.init;

import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.item.ItemOverheadWireCoil;
import elec332.core.api.registration.IItemRegister;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

import static com.technicalitiesmc.electricity.init.BlockRegister.*;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class ItemRegister implements IItemRegister {

	public static Item wireCoil;
	public static Item bundledWire;

	@Override
	public void register(IForgeRegistry<Item> registry) {
		registry.register(createItemBlock(generator));
		registry.register(createItemBlock(receiver));
		registry.register(wireCoil = new ItemOverheadWireCoil("ovhwirecoil"));
		registry.register(bundledWire = new ItemBundledWire("bundled_wire", electric_bundled_wire));
	}

	@SuppressWarnings("all")
	private Item createItemBlock(Block block){
		return new ItemBlock(block).setRegistryName(block.getRegistryName());
	}

}
