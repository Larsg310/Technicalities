package com.technicalitiesmc.util.block;

import com.technicalitiesmc.util.item.ItemResource.IResource;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;

public class BlockResource extends BlockBase {

    private static final IProperty<Integer> PROPERTY = PropertyInteger.create("meta", 0, 15);

    private final IResource[] resources;
    private final int offset;

    public BlockResource(Material material, IResource[] resources, int offset) {
        super(material);
        this.resources = resources;
        this.offset = offset;
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROPERTY);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROPERTY);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROPERTY, meta);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

}
