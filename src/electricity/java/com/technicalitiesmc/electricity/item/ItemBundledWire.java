package com.technicalitiesmc.electricity.item;

import com.google.common.collect.ImmutableList;
import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.init.ItemRegister;
import com.technicalitiesmc.electricity.proxies.TKEClientProxy;
import com.technicalitiesmc.electricity.tile.TileBundledElectricWire;
import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import com.technicalitiesmc.lib.item.ItemBlockBase;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class ItemBundledWire extends ItemBlockBase implements INoJsonItem {

    public ItemBundledWire(String string, Block block){
        super(block);
        setRegistryName(new TKEResourceLocation(string));
        setUnlocalizedNameFromName();
        setCreativeTab(TKElectricity.creativeTab);
    }

    @Override
    public void getSubItemsC(@Nonnull Item item, List<ItemStack> subItems, CreativeTabs creativeTab) {
        if (!isInCreativeTab(creativeTab)){
            return;
        }
        for (EnumDyeColor color : EnumDyeColor.values()){
            subItems.add(withCables(color));
        }
    }

    @Override
    public void addInformationC(@Nonnull ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        for (EnumDyeColor color : getColorsFromStack(stack)){
            tooltip.add(color.getDyeColorName()); //todo localize
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
        boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (ret){
            TileEntity tile = WorldHelper.getTileAt(world, pos);
            if (tile != null){
                TileBundledElectricWire wire = (TileBundledElectricWire) tile;
                wire.setColors(getColorsFromStack(stack));
            }
        }
        return ret;
    }

    public static ItemStack withCables(@Nonnull EnumDyeColor color1, EnumDyeColor... colors){
        return withCables(EnumSet.of(color1, colors));
    }

    public static ItemStack withCables(@Nonnull Collection<EnumDyeColor> colors){
        int clr = 0;
        EnumSet<EnumDyeColor> clrs = EnumSet.copyOf(colors);
        for (EnumDyeColor dye : clrs){
            clr = ColorHelper.addWire(dye, clr);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("clrwr", clr);
        ItemStack ret = new ItemStack(ItemRegister.bundledWire, 1, 0);
        ret.setTagCompound(tag);
        return ret;
    }

    public static List<EnumDyeColor> getColorsFromStack(@Nonnull ItemStack stack){
        if (stack.getItem() != ItemRegister.bundledWire){
            throw new IllegalArgumentException();
        }
        if (stack.getTagCompound() == null){
            return ImmutableList.of(EnumDyeColor.WHITE); // -_- Thx JEI
        }
        int i = stack.getTagCompound().getInteger("clrwr");
        if (i == 0){
            throw new IllegalArgumentException();
        }
        return ColorHelper.getColors(i);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getItemModel(ItemStack itemStack, World world, EntityLivingBase entityLivingBase) {
        return TKEClientProxy.modelCacheElectricWire.getModel(itemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerTextures(IIconRegistrar iIconRegistrar) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels(IElecQuadBakery iElecQuadBakery, IElecModelBakery iElecModelBakery, IElecTemplateBakery iElecTemplateBakery) {
    }

}
