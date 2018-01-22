package com.technicalitiesmc.base.proxies;

import com.google.common.base.Throwables;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.base.client.EmptyModelLoader;
import com.technicalitiesmc.base.manual.api.ManualAPI;
import com.technicalitiesmc.base.manual.api.prefab.manual.ResourceContentProvider;
import com.technicalitiesmc.base.manual.api.prefab.manual.TextureTabIconRenderer;
import com.technicalitiesmc.base.manual.client.manual.provider.*;
import com.technicalitiesmc.base.manual.common.api.ManualAPIImpl;
import com.technicalitiesmc.lib.client.SpecialRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;

public class TKClientProxy extends TKCommonProxy {
    @Override
    public void preInit() {
        super.preInit();
        ModelLoaderRegistry.registerLoader(EmptyModelLoader.INSTANCE);
    }

    @Override
    public void init() {
        super.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(Technicalities.instance, Technicalities.guiHandler);
        initManual();
    }

    private void initManual() {
        ManualAPI.addProvider(new DefinitionPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider(Technicalities.MODID, "docs/", false));
        ManualAPI.addProvider(new ResourceContentProvider(Technicalities.MODID, "docs_tldr/", true));
        ManualAPI.addProvider("", new TextureImageProvider());
        ManualAPI.addProvider("item", new ItemImageProvider());
        ManualAPI.addProvider("block", new BlockImageProvider());
        ManualAPI.addProvider("oredict", new OreDictImageProvider());
        ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(Technicalities.MODID, "textures/gui/manual_home.png")), "technicalities.manual.home", "%LANGUAGE%/index.md");

        final ResourceLocation enterTLDRMode = new ResourceLocation(Technicalities.MODID, "textures/gui/tldr.png");
        final ResourceLocation exitTLDRMode = new ResourceLocation(Technicalities.MODID, "textures/gui/tldr_exit.png");
        ManualAPI.addTab(new PageDependentTabProvider(
            i -> ManualAPIImpl.isTLDRMode() ? exitTLDRMode : enterTLDRMode,
            i -> ManualAPIImpl.isTLDRMode() ? "technicalities.manual.tldr_exit" : "technicalities.manual.tldr",
            i -> {
                ManualAPIImpl.toggleTLDRMode();
                return i;
            }
        ));
    }

    @Override
    public void registerItemModel(Item item, int meta, ModelResourceLocation location) {
        ModelLoader.setCustomModelResourceLocation(item, meta, location);
    }

    @Override
    public void bindSpecialRenderers(ASMDataTable asmDataTable) {
        for (ASMData data : asmDataTable.getAll(SpecialRenderer.class.getName())) {
            try {
                bindTileEntitySpecialRenderer(Class.forName(data.getClassName()), (TileEntitySpecialRenderer<?>) Class
                    .forName(((Type) data.getAnnotationInfo().get("value")).getClassName()).newInstance());
            } catch (Exception ex) {
                throw Throwables.propagate(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends TileEntity> void bindTileEntitySpecialRenderer(Class<?> tileType, TileEntitySpecialRenderer<?> tesr) {
        ClientRegistry.bindTileEntitySpecialRenderer((Class<T>) tileType, (TileEntitySpecialRenderer<T>) tesr);
    }

    @Override
    public World getWorld() {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public boolean isGamePaused() {
        return Minecraft.getMinecraft().isGamePaused();
    }

    @Override
    public InputStream readResource(ResourceLocation path) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(path).getInputStream();
    }
}
