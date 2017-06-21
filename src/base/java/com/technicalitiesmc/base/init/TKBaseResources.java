package com.technicalitiesmc.base.init;

import java.io.File;

import com.technicalitiesmc.Technicalities;
import com.technicalitiesmc.base.item.ItemHammer;
import com.technicalitiesmc.lib.TKLib.Resource.Provider;
import com.technicalitiesmc.lib.TKLib.Resource.Type;
import com.technicalitiesmc.lib.resource.ResourceManager;
import com.technicalitiesmc.lib.resource.ResourceProvider;
import com.technicalitiesmc.lib.resource.ResourceType;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TKBaseResources {

    private static final File CONFIG_DIR = new File("./config/" + Technicalities.MODID + "/resources/");
    private static final ResourceLocation CLIENT_CONFIG_DIR = new ResourceLocation(Technicalities.MODID, "resources");

    public static ResourceType copper;
    public static ResourceType lead;
    public static ResourceType nickel;
    public static ResourceType platinum;
    public static ResourceType silver;
    public static ResourceType tin;
    public static ResourceType zinc;

    public static ResourceProvider.Item hammer;

    public static void initialize() {
        copper = new ResourceType.Simple(CONFIG_DIR, CLIENT_CONFIG_DIR);
        lead = new ResourceType.Simple(CONFIG_DIR, CLIENT_CONFIG_DIR);
        nickel = new ResourceType.Simple(CONFIG_DIR, CLIENT_CONFIG_DIR);
        platinum = new ResourceType.Simple(CONFIG_DIR, CLIENT_CONFIG_DIR);
        silver = new ResourceType.Simple(CONFIG_DIR, CLIENT_CONFIG_DIR);
        tin = new ResourceType.Simple(CONFIG_DIR, CLIENT_CONFIG_DIR);
        zinc = new ResourceType.Simple(CONFIG_DIR, CLIENT_CONFIG_DIR);

        hammer = new ResourceProvider.Item("hammer", ItemHammer::new);
    }

    public static void register() {
        GameRegistry.register(copper.setRegistryName("copper"));
        GameRegistry.register(lead.setRegistryName("lead"));
        GameRegistry.register(nickel.setRegistryName("nickel"));
        GameRegistry.register(platinum.setRegistryName("platinum"));
        GameRegistry.register(silver.setRegistryName("silver"));
        GameRegistry.register(tin.setRegistryName("tin"));
        GameRegistry.register(zinc.setRegistryName("zinc"));

        GameRegistry.register(hammer.setRegistryName("hammer"));

        registerMetals();

        ResourceManager.INSTANCE.completeRegistration();
    }

    private static void registerMetals() {
        ResourceProvider<?>[] providersIronGold = new ResourceProvider[] { Provider.PLATE, Provider.DIRTY_GRAVEL, Provider.CLEAN_GRAVEL,
                Provider.PEBBLES, Provider.DUST, Provider.FINE_DUST, hammer };

        Type.IRON.with(providersIronGold);
        Type.GOLD.with(providersIronGold);

        ResourceProvider<?>[] providers = new ResourceProvider[] { Provider.INGOT, Provider.NUGGET, Provider.PLATE, Provider.DIRTY_GRAVEL,
                Provider.CLEAN_GRAVEL, Provider.PEBBLES, Provider.DUST, Provider.FINE_DUST, hammer };

        copper.with(providers);
        lead.with(providers);
        nickel.with(providers);
        platinum.with(providers);
        silver.with(providers);
        tin.with(providers);
        zinc.with(providers);
    }

}
