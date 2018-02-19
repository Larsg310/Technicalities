package com.technicalitiesmc.electricity.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.technicalitiesmc.api.electricity.EnumElectricityType;
import com.technicalitiesmc.electricity.TKElectricity;
import com.technicalitiesmc.electricity.init.ItemRegister;
import com.technicalitiesmc.electricity.proxies.TKEClientProxy;
import com.technicalitiesmc.electricity.tile.TileBundledElectricWire;
import com.technicalitiesmc.electricity.wires.WireColor;
import com.technicalitiesmc.electricity.wires.WireColorHelper;
import com.technicalitiesmc.electricity.wires.ground.WirePart;
import com.technicalitiesmc.lib.RayTraceHelper;
import com.technicalitiesmc.lib.item.ItemBlockBase;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.compat.jei.IHasSpecialSubtypes;
import elec332.core.util.PlayerHelper;
import elec332.core.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class ItemBundledWire extends ItemBlockBase implements IHasSpecialSubtypes, INoJsonItem {

    @SuppressWarnings("all")
    public ItemBundledWire(Block block) {
        super(block);
        setRegistryName(block.getRegistryName());
        setUnlocalizedNameFromName();
        setCreativeTab(TKElectricity.creativeTab);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getIdentifier(@Nonnull ItemStack itemStack) {
        return getColorsFromStack(itemStack).toString();
    }

    @Override
    public void addInformationC(@Nonnull ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        Pair<Integer, List<WireColor>> data = getColorsFromStack(stack);
        tooltip.add("Size: " + data.getLeft());
        for (WireColor color : data.getRight()) {
            tooltip.add(color.getType() + "  " + color.getColor().getDyeColorName()); //todo localize
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState newState) {
        WirePart wp = createWirePart(player, stack, side.getOpposite());
        boolean ret = wp != null && WorldHelper.getBlockState(world, pos.offset(side.getOpposite())).isSideSolid(world, pos.offset(side.getOpposite()), side) && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (ret) {
            TileEntity tile = WorldHelper.getTileAt(world, pos);
            if (tile != null) {
                System.out.println("addInBlPlace");
                ((TileBundledElectricWire) tile).addWire(wp);
                ((TileBundledElectricWire) tile).placed = true;
            }
        }
        return ret;
    }

    @Nullable
    private WirePart createWirePart(EntityPlayer player, ItemStack stack, EnumFacing facing) {
        Pair<Integer, List<WireColor>> data = getColorsFromStack(stack);
        WirePart wire = new WirePart(facing, data.getLeft());
        if (!wire.setColors(data.getRight())) {
            if (!player.world.isRemote) {
                PlayerHelper.sendMessageToPlayer(player, "Too many wires, please reduce the wire count in this item");
            }
            return null;
        }
        return wire;
    }

    @SubscribeEvent //Using onRightClick doesn't work if there's a block directly above the wire
    @SuppressWarnings("all")
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() != ItemRegister.bundledWire) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        if (WorldHelper.chunkLoaded(world, pos)) { //You never know...
            TileEntity tile = WorldHelper.getTileAt(world, pos);
            EnumFacing face = event.getFace();
            if (tile instanceof TileBundledElectricWire) { //attempt to add wire
                event.setUseItem(Event.Result.DENY);
                event.setCanceled(true);
                if (!world.isRemote) { //All logic on the server side
                    Pair<Vec3d, Vec3d> vec = RayTraceHelper.getRayTraceVectors(player);
                    RayTraceResult hit = WorldHelper.getBlockState(world, pos).collisionRayTrace(world, pos, vec.getLeft(), vec.getRight());
                    if (hit != null) { //Can be null
                        WirePart wire = ((TileBundledElectricWire) tile).getWire(EnumFacing.VALUES[hit.subHit]);
                        if (wire != null && wire.addWires(getColorsFromStack(stack)) && !PlayerHelper.isPlayerInCreative(player)) {
                            stack.shrink(1);
                        }
                    }
                }
            } else if (face != null) { //attempt to place at face
                IBlockState state = WorldHelper.getBlockState(world, pos);
                if (state.isSideSolid(world, pos, face)) {
                    tile = WorldHelper.getTileAt(world, pos.offset(face));
                    if (tile instanceof TileBundledElectricWire) {
                        event.setUseItem(Event.Result.DENY);
                        event.setCanceled(true);
                        if (!world.isRemote) { //All logic on the server side
                            EnumFacing rf = face.getOpposite();
                            if (((TileBundledElectricWire) tile).getWire(rf) == null) {
                                WirePart wire = createWirePart(player, stack, rf);
                                if (((TileBundledElectricWire) tile).addWire(wire) && !PlayerHelper.isPlayerInCreative(player)) {
                                    stack.shrink(1);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public static ItemStack withCables(int size, @Nonnull WireColor color1, WireColor... colors) {
        Set<WireColor> r = Sets.newHashSet(colors);
        r.add(color1);
        return withCables(r, size);
    }

    public static ItemStack withCables(@Nonnull Collection<WireColor> colors, int size) {
        int clr = 0;
        Set<WireColor> clrs = Sets.newHashSet(colors);
        for (WireColor dye : clrs) {
            clr = WireColorHelper.addWire(dye, clr);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("clrwr", clr);
        tag.setInteger("clrsz", size);
        ItemStack ret = new ItemStack(ItemRegister.bundledWire, 1, 0);
        ret.setTagCompound(tag);
        return ret;
    }

    public static Pair<Integer, List<WireColor>> getColorsFromStack(@Nonnull ItemStack stack) {
        if (stack.getItem() != ItemRegister.bundledWire) {
            throw new IllegalArgumentException();
        }
        if (stack.getTagCompound() == null) {
            return Pair.of(1, ImmutableList.of(WireColor.getWireColor(EnumDyeColor.WHITE, EnumElectricityType.AC))); // -_- Thx JEI
        }
        int i = stack.getTagCompound().getInteger("clrwr");
        int s = stack.getTagCompound().getInteger("clrsz");
        if (i == 0) {
            throw new IllegalArgumentException();
        }
        return Pair.of(s, WireColorHelper.getColors(i));
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
