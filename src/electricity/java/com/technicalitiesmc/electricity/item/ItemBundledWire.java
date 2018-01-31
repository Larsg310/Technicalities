package com.technicalitiesmc.electricity.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.init.ItemRegister;
import com.technicalitiesmc.electricity.proxies.TKEClientProxy;
import com.technicalitiesmc.electricity.tile.TileBundledElectricWire;
import com.technicalitiesmc.electricity.tile.WirePart;
import com.technicalitiesmc.electricity.util.ColorHelper;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import com.technicalitiesmc.electricity.util.WireColor;
import com.technicalitiesmc.lib.item.ItemBlockBase;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.compat.jei.IHasSpecialSubtypes;
import elec332.core.util.ItemStackHelper;
import elec332.core.util.PlayerHelper;
import elec332.core.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class ItemBundledWire extends ItemBlockBase implements INoJsonItem, IHasSpecialSubtypes {

    public ItemBundledWire(String string, Block block){
        super(block);
        setRegistryName(new TKEResourceLocation(string));
        setUnlocalizedNameFromName();
        setCreativeTab(TKElectricity.creativeTab);
        MinecraftForge.EVENT_BUS.register(new Object(){

            @SubscribeEvent //Using onRightClick doesn't work if there's a block directly above the wire
            public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
                if (event.getItemStack().getItem() != ItemRegister.bundledWire){
                    return;
                }
                /*World world = event.getWorld();
                BlockPos pos = event.getPos();
                if (WorldHelper.chunkLoaded(world, pos)){ //You never know...
                    TileEntity tile = WorldHelper.getTileAt(world, pos);
                    if (tile instanceof TileBundledElectricWire){
                        event.setUseItem(Event.Result.DENY);
                        event.setCanceled(true);
                        if (!world.isRemote) {
                            ItemStack stack = event.getEntityPlayer().getHeldItem(event.getHand());
                            if (((TileBundledElectricWire) tile).addWires(getColorsFromStack(stack)) && !PlayerHelper.isPlayerInCreative(event.getEntityPlayer())) {
                                stack.shrink(1);
                            }
                        }
                    }
                }*/

            }

        });
    }

    @Override
    public String getIdentifier(@Nonnull ItemStack itemStack) {
        return getColorsFromStack(itemStack).toString();
    }

    @Override
    public void addInformationC(@Nonnull ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        for (WireColor color : getColorsFromStack(stack)){
            tooltip.add(color.getType() + "  " + color.getColor().getDyeColorName()); //todo localize
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
        boolean ret = WorldHelper.getBlockState(world, pos.offset(side.getOpposite())).isSideSolid(world, pos, side) && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (ret){
            TileEntity tile = WorldHelper.getTileAt(world, pos);
            if (tile != null){
                TileBundledElectricWire wire = (TileBundledElectricWire) tile;
                WirePart wp = new WirePart(side);
                wp.setColors(getColorsFromStack(stack));
                wire.wires.add(wp);
                //wire.setColors(getColorsFromStack(stack));
            }
        }
        return ret;
    }

    public static ItemStack withCables(@Nonnull WireColor color1, WireColor... colors){
        Set<WireColor> r = Sets.newHashSet(colors);
        r.add(color1);
        return withCables(r);
    }

    public static ItemStack withCables(@Nonnull Collection<WireColor> colors){
        int clr = 0;
        Set<WireColor> clrs = Sets.newHashSet(colors);
        for (WireColor dye : clrs){
            clr = ColorHelper.addWire(dye, clr);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("clrwr", clr);
        ItemStack ret = new ItemStack(ItemRegister.bundledWire, 1, 0);
        ret.setTagCompound(tag);
        return ret;
    }

    public static List<WireColor> getColorsFromStack(@Nonnull ItemStack stack){
        if (stack.getItem() != ItemRegister.bundledWire){
            throw new IllegalArgumentException();
        }
        if (stack.getTagCompound() == null){
            return ImmutableList.of(WireColor.getWireColor(EnumDyeColor.WHITE, EnumElectricityType.AC)); // -_- Thx JEI
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
