package com.technicalitiesmc.electricity.client;

import com.technicalitiesmc.api.electricity.IEnergyObject;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.electricity.wires.overhead.Wire;
import elec332.core.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

/**
 * Created by Elec332 on 7-11-2017.
 */
@SideOnly(Side.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public void renderStuff(RenderWorldLastEvent event) {
        int world = WorldHelper.getDimID(Minecraft.getMinecraft().world);
        for (IEnergyObject o : Technicalities.electricityGridHandler.wirez) {
            System.out.println(o + "");
            Wire wire = (Wire) o;
            if (!wire.isOverhead() || wire.getConnectionPoint(0).getWorld() != world) {
                continue;
            }
            GlStateManager.pushMatrix();
            Vec3d tr = RenderFunctions.getTranslationForRendering(wire.getStart());
            GlStateManager.translate(tr.x, tr.y, tr.z);
            GlStateManager.glLineWidth(3);
            RenderFunctions.renderWire(wire.getStart(), wire.getEnd(), Color.BLACK, 1, false);
            GlStateManager.popMatrix();
        }
        /*Minecraft.getMinecraft().world.getChunkProvider().chunkMapping.values().forEach(new Consumer<Chunk>() {
			@Override
			public void accept(Chunk chunk) {

			}
		});*/
		/*for (TileEntity tile : Minecraft.getMinecraft().world.loadedTileEntityList){
			if (tile instanceof TileWireConnector){
				GlStateManager.pushMatrix();
				BlockPos loc = tile.getPos();
				Vec3d tr = RenderFunctions.getTranslationForRendering(loc);
				GlStateManager.translate(tr.x, tr.y, tr.z);
				//for (BlockPos pos : ((TileWireConnector) tile).getConnectors()){
				//	RenderFunctions.renderWire(loc, pos, Color.BLACK, 1, true);
				//}
				GlStateManager.popMatrix();
			}
		}*/
    }

}
