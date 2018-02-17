package com.technicalitiesmc.mechanical.tile;

import com.technicalitiesmc.api.mechanical.IKineticNode;
import com.technicalitiesmc.api.mechanical.IShaftAttachable;
import com.technicalitiesmc.api.util.ObjFloatConsumer;
import com.technicalitiesmc.lib.block.TileBase;
import com.technicalitiesmc.lib.client.SpecialRenderer;
import com.technicalitiesmc.mechanical.block.BlockHandCrank;
import com.technicalitiesmc.mechanical.client.renderer.TESRHandCrank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import static java.lang.Math.max;
import static java.lang.Math.min;

@SpecialRenderer(TESRHandCrank.class)
public class TileHandCrank extends TileBase implements ITickable, IKineticNode.Host {
    private float prevEnergy = 0.0f;
    private float energy = 0.0f;
    private int timeout = 0;

    private final IKineticNode node = IKineticNode.create(this);

    public void crank(@Nullable EntityPlayer player) {
        energy = min(1.0f, energy + getCrankAmountForPlayer(player));
        playWindUpSound(prevEnergy, energy);
        timeout = 5;
    }

    public float getCrankAmountForPlayer(@Nullable EntityPlayer player) {
        if (player instanceof FakePlayer) return 0.025f; // Nope >:D
        if (player == null) return 0.1f;
        float multiplier = 1.0f;
        PotionEffect eff = player.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("strength")));
        if (eff != null) {
            multiplier += (eff.getAmplifier() + 1) * 0.5;
        }
        return 0.1f * multiplier;
    }

    @Override
    public void update() {
        prevEnergy = energy;
        float v = node.getVelocity() * 0.0001f;
        if (timeout > 0 && v > 0) v = 0;
        energy = max(0, min(energy - v, 1));
        if (!world.isRemote) {
            boolean update = false;
            if (prevEnergy > 0 && energy == 0) {
                playStopSound();
                update = true;
            } else if (Math.round((prevEnergy) * 10) != Math.round((energy) * 10)) {
                playWindDownSound(1 - energy);
                update = true;
            }
            if (update) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setFloat("e", energy);
                sendPacket(1, nbt);
            }
        }
        if (timeout != 0) timeout--;

        if (!world.isRemote) {
            updateScheduler();
        }
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        switch (id) {
            case 1:
                energy = tag.getFloat("e");
                break;
            default:
                node.deserializeNBT(tag.getCompoundTag("node"));
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setFloat("e", energy);
        nbt.setInteger("t", timeout);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = prevEnergy = nbt.getFloat("e");
        timeout = nbt.getInteger("t");
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("node", node.serializeNBT());
        return tag;
    }

    public float getCrankRotation(float partialTicks) {
        return prevEnergy + (energy - prevEnergy) * partialTicks;
    }

    @Override
    public float getInertia() {
        return 0.1f;
    }

    @Override
    public float getAppliedPower() {
        return timeout > 0 ? 0 : 20 * energy;
    }

    @Override
    public float getConsumedPower() {
        return energy >= 1 && node.getVelocity() < 0 ? Float.POSITIVE_INFINITY : getInertia();
    }

    @Override
    public void addNeighbors(ObjFloatConsumer<IKineticNode> neighbors, BiPredicate<World, BlockPos> posValidator) {
        EnumFacing dir = getFacing();
        IKineticNode.findShaft(getWorld(), getPos(), dir, 1, neighbors, posValidator);
    }

    @Override
    public World getKineticWorld() {
        return getWorld();
    }

    @Override
    public ChunkPos getKineticChunk() {
        return new ChunkPos(getPos());
    }

    @Override
    public void validate() {
        node.validate(getWorld().isRemote);
        super.validate();
    }

    @Override
    public void onLoad() {
        node.validate(getWorld().isRemote);
        super.onLoad();
    }

    @Override
    public void invalidate() {
        node.invalidate();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        node.invalidate();
        super.onChunkUnload();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null || super.hasCapability(capability, facing);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == IShaftAttachable.CAPABILITY && facing == getFacing())
            return (T) (IShaftAttachable) () -> node;
        else return super.getCapability(capability, facing);
    }

    private EnumFacing getFacing() {
        return getWorld().getBlockState(getPos()).getValue(BlockHandCrank.PROP_FACING);
    }

    public void playWindUpSound(float from, float to) {
        int clickAmt = 15;
        float pitchStep = 1f / clickAmt;
        int delay = 0;
        for (int i = 0; i < clickAmt; i++) {
            float v = i / (float) clickAmt;
            if (v >= from && v <= to) {
                final int j = i;
                schedule(delay++,
                    () -> world.playSound(null, getPos(),
                        SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF,
                        SoundCategory.BLOCKS, 1.0f, 1.0f + j * pitchStep));
            }
        }
    }

    public void playWindDownSound(float energy) {
        world.playSound(null, getPos(),
            SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF,
            SoundCategory.BLOCKS, 0.5f, 2.0f - energy * 0.5f);
        schedule(2,
            () -> world.playSound(null, getPos(),
                SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF,
                SoundCategory.BLOCKS, 0.2f, 1.5f - energy * 0.5f)
        );
        schedule(3,
            () -> world.playSound(null, getPos(),
                SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF,
                SoundCategory.BLOCKS, 0.2f, 1.0f - energy * 0.5f)
        );
    }

    public void playStopSound() {
        world.playSound(null, getPos(),
            SoundEvents.BLOCK_STONE_STEP,
            SoundCategory.BLOCKS, 0.2f, 1.2f);
    }

    // ---- Scheduler stuff ---- \\
    // ----  (for  sounds)  ---- \\

    // is there a better way to do delayed operations? >.>
    // we desperately need MAIL
    private List<Pair<IntPtr, Runnable>> pendingOperations = new ArrayList<>();

    private void schedule(int delayTicks, Runnable op) {
        if (!world.isRemote) pendingOperations.add(Pair.of(new IntPtr(delayTicks), op));
    }

    private void updateScheduler() {
        List<Pair<IntPtr, Runnable>> remove = new ArrayList<>();
        pendingOperations.stream()
            .filter(it -> it.getLeft().value == 0)
            .forEach(remove::add);
        remove.forEach(it -> it.getRight().run());
        pendingOperations.removeAll(remove);
        pendingOperations.forEach(it -> it.getLeft().value--);
    }

    private static class IntPtr {
        public int value;

        IntPtr(int value) {
            this.value = value;
        }
    }
}
