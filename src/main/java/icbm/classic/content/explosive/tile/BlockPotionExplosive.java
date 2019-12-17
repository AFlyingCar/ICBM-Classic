package icbm.classic.content.explosive.tile;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BlockPotionExplosive extends BlockExplosive {
    private PotionEffect effect;

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

    /*
    // Allow right clicking the explosive block with a new potion
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof TileEntityExplosive) {
                TileEntityExplosive tileEntityExplosive = (TileEntityExplosive) tile;
                ItemStack itemStack = playerIn.getHeldItem(hand);

                if(itemStack.getItem() == Items.POTIONITEM || itemStack.getItem() == Items.LINGERING_POTION) {
                    PotionEffect oldEffect = getEffectFromTileEntity(worldIn, pos);

                    List<PotionEffect> newEffects = PotionUtils.getEffectsFromStack(itemStack);

                    if(!newEffects.isEmpty() && newEffects.get(0).equals(oldEffect)) {
                        // Replace effect on tile entity
                        newEffects.get(0).writeCustomPotionEffectToNBT(tileEntityExplosive.nbtData.getCompoundTag("effect"));

                        // Spawn a new potion stack entity in the world
                        ItemStack oldItemStack = new ItemStack(Items.POTIONITEM, 1);
                        PotionUtils.addPotionToItemStack(oldItemStack, oldEffect.getPotion().); //.requireNonNull(PotionType.getPotionTypeForName(effect.getPotion().getName())));
                        PotionUtils.appendEffects(oldItemStack, Collections.singleton(oldEffect));

                        worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), oldItemStack));

                        // Remove the potion from our hand
                        playerIn.setHeldItem(hand, ItemStack.EMPTY);
                    }
                }
            }
        }

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }*/

    private PotionEffect getEffectFromTileEntity(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(!(te instanceof TileEntityExplosive))
            return null;

        TileEntityExplosive tileEntityExplosive = (TileEntityExplosive)te;

        if(!tileEntityExplosive.nbtData.hasKey("effect"))
            return null;

        return PotionEffect.readCustomPotionEffectFromNBT(tileEntityExplosive.nbtData.getCompoundTag("effect"));
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        effect = getEffectFromTileEntity(world, pos);

        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);

        for(ItemStack stack : drops) {
            if(stack.getItem() instanceof ItemBlockPotionExplosive) {
                stack.setItemDamage(Explosives.POTION.ordinal());
                PotionUtils.addPotionToItemStack(stack, Objects.requireNonNull(PotionType.getPotionTypeForName(effect.getPotion().getName())));
                PotionUtils.appendEffects(stack, Collections.singleton(effect));
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        TileEntity te = super.createNewTileEntity(world, meta);
        // TODO: Set NBT data on te for potion effect

        return te;
    }
}
