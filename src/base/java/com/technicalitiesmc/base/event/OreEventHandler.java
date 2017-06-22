package com.technicalitiesmc.base.event;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.lib.TKLib;
import com.technicalitiesmc.lib.capability.SimpleCapability;
import com.technicalitiesmc.lib.item.ItemBlockResource;
import com.technicalitiesmc.lib.item.ItemResource;
import com.technicalitiesmc.lib.resource.ResourceEntry;
import com.technicalitiesmc.lib.resource.ResourceManager;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class OreEventHandler {

    private static final Set<EntityItem> gravelEntities = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<EntityItem> oreEntities = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof EntityItem) {
            EntityItem entityItem = (EntityItem) entity;
            ItemStack stack = entityItem.getItem();
            handleStack(stack, s -> gravelEntities.add(entityItem), s -> oreEntities.add(entityItem));
        }
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject() instanceof ItemStack) {
            handleStack(event.getObject(), stack -> event.addCapability(GravelCleanCounter.NAME, new GravelCleanCounter.Provider()),
                    stack -> event.addCapability(OreFallTracker.NAME, new OreFallTracker.Provider()));
        }
    }

    private void handleStack(ItemStack stack, Consumer<ItemStack> ifDirtyGravel, Consumer<ItemStack> ifOre) {
        if (stack.getItem() instanceof ItemResource) {
            ResourceEntry<ItemStack> entry = ((ItemResource) stack.getItem()).getResources()[stack.getMetadata()];
            if (entry.getProvider() == TKLib.Resource.Provider.DIRTY_GRAVEL) {
                ifDirtyGravel.accept(stack);
            }
        } else if (stack.getItem() instanceof ItemBlockResource) {
            ResourceEntry<IBlockState> entry = ((ItemBlockResource) stack.getItem()).getResources()[stack.getMetadata()];
            if (entry.getProvider() == TKLib.Resource.Provider.ORE) {
                ifOre.accept(stack);
            }
        } else {
            for (ResourceEntry<ItemStack> entry : ResourceManager.INSTANCE.getResources(TKLib.Resource.Provider.DIRTY_GRAVEL).values()) {
                if (entry.getAbstract().test(stack)) {
                    ifDirtyGravel.accept(stack);
                    return;
                }
            }
            for (ResourceEntry<IBlockState> entry : ResourceManager.INSTANCE.getResources(TKLib.Resource.Provider.ORE).values()) {
                if (entry.getAbstract().test(stack)) {
                    ifOre.accept(stack);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.phase == Phase.START) {
            return;
        }

        gravelEntities.removeIf(entity -> {
            GravelCleanCounter counter = entity.getItem().getCapability(GravelCleanCounter.CAPABILITY, null);
            if (counter == null) {
                return true;
            }
            if (entity.isInWater()) {
                counter.water++;
                if (entity.motionX != 0 || entity.motionZ != 0) {
                    counter.flowing++;
                }
            }
            if (counter.water >= 20 * 4 && counter.flowing >= 20 * 2) {
                ResourceEntry<ItemStack> entry = (ResourceEntry<ItemStack>) entity.getItem().getCapability(ResourceEntry.CAPABILITY, null);

                ItemStack clean = ResourceManager.INSTANCE.get(TKLib.Resource.Provider.CLEAN_GRAVEL, entry.getType()).get().get().copy();
                clean.setCount(entity.getItem().getCount());
                spawnCopy(entity, clean);

                int amt = (int) Math.round((0.5 + 0.25 * Math.random()) * clean.getCount());
                if (amt > 0) {
                    ItemStack gravel = new ItemStack(Blocks.GRAVEL, amt);
                    spawnCopy(entity, gravel);
                }

                spawnParticles(entity);

                entity.setDead();
                return true;
            }
            return entity.isDead;
        });

        oreEntities.removeIf(entity -> {
            OreFallTracker tracker = entity.getItem().getCapability(OreFallTracker.CAPABILITY, null);
            if (tracker == null) {
                return true;
            }

            if (entity.isCollided) {
                if (tracker.last > 16 && !entity.isInWater() && !entity.isInLava()) {
                    Material mat = entity.world.getBlockState(entity.getPosition().down()).getMaterial();
                    if (mat != Material.ROCK && mat != Material.IRON) {
                        return entity.isDead;
                    }

                    ResourceEntry<IBlockState> entry = ResourceManager.INSTANCE.match(TKLib.Resource.Provider.ORE, entity.getItem()).get();

                    int dirty = 0, clean = 0, gravel = 0, cobble = 0;

                    for (int i = 0; i < entity.getItem().getCount(); i++) {
                        if (Math.random() < 0.05) {
                            clean++;
                            gravel++;
                        } else {
                            dirty++;
                            if (Math.random() < 0.1) {
                                dirty++;
                            }
                        }
                        if (Math.random() < 0.05) {
                            gravel++;
                        }
                        if (Math.random() < 0.1) {
                            cobble++;
                        }
                    }

                    while (dirty > 0) {
                        ItemStack stack = ResourceManager.INSTANCE.get(TKLib.Resource.Provider.DIRTY_GRAVEL, entry.getType()).get().get()
                                .copy();
                        stack.setCount(Math.min(dirty, stack.getMaxStackSize()));
                        spawnCopy(entity, stack);
                        dirty -= stack.getCount();
                    }

                    while (clean > 0) {
                        ItemStack stack = ResourceManager.INSTANCE.get(TKLib.Resource.Provider.CLEAN_GRAVEL, entry.getType()).get().get()
                                .copy();
                        stack.setCount(Math.min(clean, stack.getMaxStackSize()));
                        spawnCopy(entity, stack);
                        clean -= stack.getCount();
                    }

                    while (gravel > 0) {
                        ItemStack stack = new ItemStack(Blocks.GRAVEL);
                        stack.setCount(Math.min(gravel, stack.getMaxStackSize()));
                        spawnCopy(entity, stack);
                        gravel -= stack.getCount();
                    }

                    while (cobble > 0) {
                        ItemStack stack = new ItemStack(Blocks.COBBLESTONE);
                        stack.setCount(Math.min(cobble, stack.getMaxStackSize()));
                        spawnCopy(entity, stack);
                        cobble -= stack.getCount();
                    }

                    spawnParticles(entity);

                    entity.setDead();
                }
            } else {
                if (!entity.isInWater() && !entity.isInLava()) {
                    tracker.last = entity.fallDistance;
                } else {
                    tracker.last = 0;
                }
            }

            return entity.isDead;
        });
    }

    private void spawnParticles(EntityItem entity) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    ((WorldServer) entity.world).spawnParticle(EnumParticleTypes.CLOUD, false, entity.posX + x / 8D + entity.motionX * 2,
                            entity.posY + y / 8D + entity.motionY * 2, entity.posZ + z / 8D + entity.motionZ * 2, 1, 0D, 0D, 0D, 0D);
                }
            }
        }
    }

    private void spawnCopy(EntityItem entity, ItemStack stack) {
        EntityItem cleanItem = new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, stack);
        cleanItem.motionX = entity.motionX;
        cleanItem.motionY = entity.motionY;
        cleanItem.motionZ = entity.motionZ;
        cleanItem.rotationYaw = entity.rotationYaw;
        cleanItem.hoverStart = entity.hoverStart;
        cleanItem.setDefaultPickupDelay();
        entity.world.spawnEntity(cleanItem);
    }

    @SimpleCapability
    private static class GravelCleanCounter {

        public static final ResourceLocation NAME = new ResourceLocation(Technicalities.MODID, "gravel_clean_counter");

        @CapabilityInject(GravelCleanCounter.class)
        public static final Capability<GravelCleanCounter> CAPABILITY = null;

        private int water = 0, flowing = 0;

        private static class Provider implements ICapabilityProvider {

            private final GravelCleanCounter counter = new GravelCleanCounter();

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                return capability == CAPABILITY;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                return capability == CAPABILITY ? (T) counter : null;
            }

        }

    }

    @SimpleCapability
    private static class OreFallTracker {

        public static final ResourceLocation NAME = new ResourceLocation(Technicalities.MODID, "ore_fall_tracker");

        @CapabilityInject(OreFallTracker.class)
        public static final Capability<OreFallTracker> CAPABILITY = null;

        private float last = 0;

        private static class Provider implements ICapabilityProvider {

            private final OreFallTracker tracker = new OreFallTracker();

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                return capability == CAPABILITY;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                return capability == CAPABILITY ? (T) tracker : null;
            }

        }

    }

}
