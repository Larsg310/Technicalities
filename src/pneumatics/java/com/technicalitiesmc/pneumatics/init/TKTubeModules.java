package com.technicalitiesmc.pneumatics.init;

import com.technicalitiesmc.api.pneumatics.TubeModule;
import com.technicalitiesmc.pneumatics.TKPneumatics;
import com.technicalitiesmc.pneumatics.tube.module.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = TKPneumatics.MODID)
public class TKTubeModules {

    public static TubeModule.Type<TMColorFilter> color_filter= new TMColorFilter.Type();
    public static TubeModule.Type<TMMembrane> membrane = new TMMembrane.Type();
    public static TubeModule.Type<TMPlug> plug = new TMPlug.Type();
    public static TubeModule.Type<TMFilter> filter = new TMFilter.Type();
    public static TubeModule.Type<TMSorter> sorter = new TMSorter.Type();

    @SubscribeEvent
    public static void onModuleRegistration(RegistryEvent.Register<TubeModule.RegistryEntry> event) {
        IForgeRegistry<TubeModule.RegistryEntry> registry = event.getRegistry();

        registry.register(new TubeModule.RegistryEntry(color_filter).setRegistryName("color_filter"));
        registry.register(new TubeModule.RegistryEntry(membrane).setRegistryName("membrane"));
        registry.register(new TubeModule.RegistryEntry(plug).setRegistryName("plug"));
        registry.register(new TubeModule.RegistryEntry(filter).setRegistryName("filter"));
        registry.register(new TubeModule.RegistryEntry(sorter).setRegistryName("sorter"));
    }

}
