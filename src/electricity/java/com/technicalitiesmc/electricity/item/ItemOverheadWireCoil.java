package com.technicalitiesmc.electricity.item;

import com.technicalitiesmc.api.TechnicalitiesAPI;
import com.technicalitiesmc.api.electricity.*;
import com.technicalitiesmc.api.util.ConnectionPoint;
import com.technicalitiesmc.base.Technicalities;
import com.technicalitiesmc.electricity.util.EnumWireType;
import com.technicalitiesmc.electricity.util.TKEResourceLocation;
import com.technicalitiesmc.electricity.util.Wire;
import com.technicalitiesmc.electricity.util.WireData;
import elec332.core.item.AbstractItem;
import elec332.core.world.WorldHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 23-11-2017.
 */
public class ItemOverheadWireCoil extends AbstractItem {

	public ItemOverheadWireCoil(String name) {
		super(new TKEResourceLocation(name));
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseC(EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote){
			return EnumActionResult.SUCCESS;
		}
		ItemStack stack = player.getHeldItem(hand);
		NBTTagCompound tag = stack.getTagCompound();
		TileEntity tile = WorldHelper.getTileAt(world, pos);
		IElectricityDevice energyObject = tile == null ? null : tile.getCapability(TechnicalitiesAPI.ELECTRICITY_CAP, null);
		if (energyObject == null){
			return EnumActionResult.SUCCESS;
		}
		Vec3d hitVec = new Vec3d(hitX, hitY, hitZ);
		ConnectionPoint cp = null;
		for (IEnergyObject obj : energyObject.getInternalComponents()){
			cp = obj.getConnectionPoint(facing, hitVec);
			if (cp != null){
				break;
			}
		}
		if (cp == null){
			return EnumActionResult.SUCCESS;
		}
		if (tag == null){
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
			tag.setLong("bpL", cp.getPos().toLong());
			if (cp.getSide() != null) {
				tag.setString("bpS", cp.getSide().getName());
			}
			tag.setInteger("bpN", cp.getSideNumber());
			tag.setDouble("xH", hitVec.x);
			tag.setDouble("yH", hitVec.y);
			tag.setDouble("zH", hitVec.z);
			//PlayerHelper.sendMessageToPlayer(player, "StartWire");
		} else {
			BlockPos bp = BlockPos.fromLong(tag.getLong("bpL"));
			EnumFacing bpf = tag.hasKey("bpS") ? EnumFacing.byName(tag.getString("bpS")) : null;
			int n = tag.getInteger("bpN");
			Vec3d otherHVec = new Vec3d(tag.getDouble("xH"), tag.getDouble("yH"), tag.getDouble("zH"));
			ConnectionPoint newcp = new ConnectionPoint(bp, world, bpf, n);
			if (newcp.equals(cp)){
				//PlayerHelper.sendMessageToPlayer(player, "clearWire");
				return EnumActionResult.FAIL;
			}
			stack.setTagCompound(null);
			Wire wire = new Wire(newcp, otherHVec, cp, hitVec, new WireData(EnumWireType.TEST, WireThickness.AWG_00, WireConnectionMethod.OVERHEAD, EnumElectricityType.AC));
			Technicalities.electricityGridHandler.addWire(wire);
			//PlayerHelper.sendMessageToPlayer(player, "addedWire");
		}
		return EnumActionResult.SUCCESS;
	}

}
