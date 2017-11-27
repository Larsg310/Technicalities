package com.technicalitiesmc.electricity;

import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.api.electricity.IWireType;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.electricity.handler.PlayerEventHandler;
import com.technicalitiesmc.electricity.init.BlockRegister;
import com.technicalitiesmc.electricity.init.ElementRegister;
import com.technicalitiesmc.electricity.init.ItemRegister;
import com.technicalitiesmc.electricity.proxies.TKECommonProxy;
import com.technicalitiesmc.electricity.util.Config;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import elec332.core.api.IElecCoreMod;
import elec332.core.api.config.IConfigWrapper;
import elec332.core.api.module.IModuleController;
import elec332.core.api.network.INetworkHandler;
import elec332.core.api.network.ModNetworkHandler;
import elec332.core.api.registration.IObjectRegister;
import elec332.core.config.ConfigWrapper;
import elec332.core.inventory.window.WindowManager;
import elec332.core.util.AbstractCreativeTab;
import elec332.core.util.LoadTimer;
import elec332.core.util.RegistryHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * Created by Elec332 on 23-11-2017.
 */
@Mod(modid = TKElectricity.MODID, name = TKElectricity.MODNAME, version = TKElectricity.VERSION,
		dependencies = "required-after:" + Technicalities.MODID)
public class TKElectricity implements IModuleController, IElecCoreMod {

	public static final String MODID = "tkelectricity", MODNAME = "Technicalities Electricity", VERSION = "%VERSION%";

	@SidedProxy(serverSide = "com.technicalitiesmc.electricity.proxies.TKECommonProxy", clientSide = "com.technicalitiesmc.electricity.proxies.TKEClientProxy")
	public static TKECommonProxy proxy;

	@Mod.Instance(MODID)
	public static TKElectricity instance;
	@ModNetworkHandler
	public static INetworkHandler networkHandler;
	public static Configuration config;
	public static CreativeTabs creativeTab;
	public static Logger logger;
	public static IConfigWrapper configWrapper;
	public static IForgeRegistry<IWireType> wireTypeRegistry;

	private LoadTimer loadTimer;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = LogManager.getLogger(MODNAME);
		loadTimer = new LoadTimer(logger, MODNAME);
		loadTimer.startPhase(event);
		RegistryHelper.registerEmptyCapability(IEnergyObject.class);
		wireTypeRegistry = RegistryHelper.createRegistry(new TKEResourceLocation("wire_typr_registry"), IWireType.class, RegistryHelper.getNullCallback());
		creativeTab = AbstractCreativeTab.create(MODID, () -> new ItemStack(Blocks.ANVIL));
		config = new Configuration(Technicalities.baseFolder, "Electricity.cfg");
		configWrapper = new ConfigWrapper(config);
		configWrapper.registerConfig(new Config());
		MinecraftForge.EVENT_BUS.register(proxy);
		loadTimer.endPhase(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		loadTimer.startPhase(event);

		WindowManager.INSTANCE.register(proxy);
		MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
		proxy.initRendering();
		ElementRegister.init();

		loadTimer.endPhase(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Override
	public void registerRegisters(Consumer<IObjectRegister<?>> handler) {
		handler.accept(new BlockRegister());
		handler.accept(new ItemRegister());
	}

	@Override
	public boolean isModuleEnabled(String s) {
		return true;
	}

}
