package com.technicalitiesmc.electricity.init;

import com.google.common.collect.Sets;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.electricity.item.ItemBundledWire;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import com.technicalitiesmc.electricity.util.WireColor;
import elec332.core.api.registration.IObjectRegister;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class RecipeRegister implements IObjectRegister<IRecipe> {

    @Override
    public void register(IForgeRegistry<IRecipe> registry) {
        registry.register(new IRecipe() {

            @Override
            public boolean matches(InventoryCrafting inv, World worldIn) {
                Set<WireColor> colors = Sets.newHashSet();
                int items = 0;
                for (ItemStack stack : inv.stackList){
                    if (stack.isEmpty()){
                        continue;
                    }
                    if (stack.getItem() == ItemRegister.bundledWire){
                        List<WireColor> c = ItemBundledWire.getColorsFromStack(stack);
                        for (WireColor color : c){
                            if (colors.contains(color)){
                                return false;
                            }
                        }
                        colors.addAll(c);
                        items++;
                        continue;
                    }
                    return false;
                }
                return items > 1;
            }

            @Override
            public ItemStack getCraftingResult(InventoryCrafting inv) {
                Set<WireColor> colors = Sets.newHashSet();
                for (ItemStack stack : inv.stackList){
                    if (stack.isEmpty()){
                        continue;
                    }
                    if (stack.getItem() == ItemRegister.bundledWire){
                        colors.addAll(ItemBundledWire.getColorsFromStack(stack));
                    }
                }
                return ItemBundledWire.withCables(colors);
            }

            @Override
            public boolean canFit(int width, int height) {
                return true;
            }

            @Override
            public ItemStack getRecipeOutput() {
                return ItemBundledWire.withCables(WireColor.getWireColor(EnumDyeColor.WHITE, EnumElectricityType.AC));
            }

            @Override
            public IRecipe setRegistryName(ResourceLocation name) {
                throw new UnsupportedOperationException();
            }

            @Nullable
            @Override
            public ResourceLocation getRegistryName() {
                return new TKEResourceLocation("reco[e");
            }

            @Override
            public Class<IRecipe> getRegistryType() {
                return IRecipe.class;
            }

        });
    }

    @Override
    public Class<IRecipe> getType() {
        return IRecipe.class;
    }

}
