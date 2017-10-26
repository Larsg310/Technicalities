package com.technicalitiesmc.pneumatics.tube.module;

import com.technicalitiesmc.api.pneumatics.EnumTubeDirection;
import com.technicalitiesmc.api.pneumatics.IPneumaticTube;
import com.technicalitiesmc.api.pneumatics.ITubeStack;
import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.util.Tint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;

public class TMColorFilter extends TubeModule {

    private static final ResourceLocation PATH_SINGLE = new ResourceLocation(TKPneumatics.MODID, "block/tube/color_filter/single");

    private EnumDyeColor color = EnumDyeColor.WHITE;

    private TMColorFilter(IPneumaticTube tube, EnumFacing side) {
        super(tube, side);
    }

    @Override
    public boolean requiresConnection() {
        return true;
    }

    @Override
    public boolean preventsConnection() {
        return false;
    }

    @Override
    public boolean renderTube() {
        return true;
    }

    @Override
    public ResourceLocation getModel() {
        return PATH_SINGLE;
    }

    @Override
    public int getTint(int index) {
        return Tint.getColor(color).getRGB();
    }

    @Override
    public boolean canStackTraverse(ITubeStack stack, EnumTubeDirection direction) {
        return stack.getColor() == color;
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.DYE) {
            color = EnumDyeColor.byDyeDamage(stack.getMetadata());
            markDirty();
            return true;
        } else if (stack.getItem() == Item.getItemFromBlock(Blocks.WOOL)) {
            color = EnumDyeColor.byMetadata(stack.getMetadata());
            markDirty();
            return true;
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setByte("color", (byte) color.getMetadata());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        color = EnumDyeColor.byMetadata(tag.getByte("color") & 0xFF);
    }

    @Override
    public NBTTagCompound writeDescription(NBTTagCompound tag) {
        tag.setByte("color", (byte) color.getMetadata());
        return tag;
    }

    @Override
    public void readDescription(NBTTagCompound tag) {
        color = EnumDyeColor.byMetadata(tag.getByte("color") & 0xFF);
    }

    @Override
    public void writeUpdateExtra(PacketBuffer buf) {
        buf.writeEnumValue(color);
    }

    @Override
    public void readUpdateExtra(PacketBuffer buf) {
        color = buf.readEnumValue(EnumDyeColor.class);
    }

    public static class Type extends TubeModule.Type<TMColorFilter> {

        @Override
        public TMColorFilter placeSingle(IPneumaticTube tube, EnumFacing side) {
            return new TMColorFilter(tube, side);
        }

        @Override
        public Pair<TMColorFilter, TMColorFilter> placePair(IPneumaticTube tube, EnumFacing side, IPneumaticTube other) {
            return Pair.of(new TMColorFilter(tube, side), null);
        }

        @Override
        public TMColorFilter instantiate(IPneumaticTube tube, EnumFacing side) {
            return new TMColorFilter(tube, side);
        }

        @Override
        public void link(TMColorFilter filter1, TMColorFilter filter2) {
        }

        @Override
        public void registerModels(Consumer<ResourceLocation> registry) {
            registry.accept(PATH_SINGLE);
        }

    }

}
