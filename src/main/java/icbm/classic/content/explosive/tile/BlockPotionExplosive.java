package icbm.classic.content.explosive.tile;

import icbm.classic.ICBMClassic;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class BlockPotionExplosive extends BlockExplosive {
    public BlockPotionExplosive() {
        super("potionexplosives", Material.TNT);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityExplosive) {
            List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);

            // We will only use one potion effect
            if(!effects.isEmpty()) {
                NBTTagCompound effectTag = new NBTTagCompound();
                effects.get(0).writeCustomPotionEffectToNBT(effectTag);
                ((TileEntityExplosive) tile).nbtData.setTag("effect", effectTag);
            }
        }

        super.onBlockPlacedBy(world, pos, state, entityLiving, stack);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        TileEntity te = super.createNewTileEntity(world, meta);
        // TODO: Set NBT data on te for potion effect

        return te;
    }
}
