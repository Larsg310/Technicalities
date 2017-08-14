package com.technicalitiesmc.api.pneumatics;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.technicalitiesmc.lib.inventory.SimpleContainer;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class TubeModule {

    private static final BiConsumer<TubeModule, Consumer<PacketBuffer>> sendToServer = null;

    private final IPneumaticTube tube;
    private EnumFacing side;

    public TubeModule(IPneumaticTube tube, EnumFacing side) {
        this.tube = tube;
        this.side = side;
    }

    public final IPneumaticTube getTube() {
        return tube;
    }

    public final EnumFacing getSide() {
        return side;
    }

    public final void setSide(EnumFacing side) {
        this.side = side;
    }

    public abstract boolean requiresConnection();

    public abstract boolean preventsConnection();

    public abstract boolean renderTube();

    public abstract ResourceLocation getModel();

    public int getTint(int index) {
        return 0xFFFFFFFF;
    }

    public boolean canStackTraverse(ITubeStack stack, EnumTubeDirection direction) {
        return true;
    }

    public ITubeStack traverse(ITubeStack stack, EnumTubeDirection direction) {
        return stack;
    }

    public float getPosition() {
        return 0.5F;
    }

    public int getTraversalPriority(ITubeStack stack) {
        return 0;
    }

    public boolean onActivated(EntityPlayer player, EnumHand hand) {
        return false;
    }

    protected final void save() {
        tube.save();
    }

    protected final void markDirty() {
        save();
        sync();
    }

    protected final void sync() {
        tube.sync();
    }

    protected void sendToServer(Consumer<PacketBuffer> writer) {
        sendToServer.accept(this, writer);
    }

    public void handleClientPacket(PacketBuffer buf) {

    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
    }

    public NBTTagCompound writeDescription(NBTTagCompound tag) {
        return tag;
    }

    public void readDescription(NBTTagCompound tag) {
    }

    public void writeUpdateExtra(PacketBuffer buf) {
    }

    public void readUpdateExtra(PacketBuffer buf) {
    }

    public static abstract class ContainerModule extends TubeModule {

        private static final BiConsumer<TubeModule.ContainerModule, EntityPlayer> openGui = null;

        public ContainerModule(IPneumaticTube tube, EnumFacing side) {
            super(tube, side);
        }

        public abstract SimpleContainer createContainer(EntityPlayer player);

        @SideOnly(Side.CLIENT)
        public abstract GuiContainer createGUI(EntityPlayer player);

        protected final void openGUI(EntityPlayer player) {
            openGui.accept(this, player);
        }

    }

    public static abstract class Type<T extends TubeModule> extends IForgeRegistryEntry.Impl<Type<T>> {

        public abstract T placeSingle(IPneumaticTube tube, EnumFacing side);

        public abstract Pair<T, T> placePair(IPneumaticTube tube, EnumFacing side, IPneumaticTube other);

        public abstract T instantiate(IPneumaticTube tube, EnumFacing side);

        public abstract void link(T module1, T module2);

        public abstract void registerModels(Consumer<ResourceLocation> registry);

    }

}
