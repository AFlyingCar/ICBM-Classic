package icbm.classic.content.explosive.handlers;

import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.explosive.blast.Blast;
import icbm.classic.content.explosive.blast.BlastPotion;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Supplier;

public class ExPotion extends Explosion {
    public ExPotion(String mingZi, EnumTier tier) {
        super(mingZi, tier);

    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale) {
        EntityExplosive explosive = null;
        if(entity instanceof EntityExplosive)
            explosive = (EntityExplosive)entity;

        if(explosive == null) {
            System.err.println("Explosive is null! We cannot find the potion effect!");
            return;
        }

        PotionEffect effect = null;
        if(explosive.nbtData.hasKey("effect"))
            effect = PotionEffect.readCustomPotionEffectFromNBT(explosive.nbtData.getCompoundTag("effect"));

        float r = 1, g = 1, b = 1;
        if(effect != null) {
            int color = effect.getPotion().getLiquidColor();
            r = (float)(color >> 16 & 0xff) / 255f;
            g = (float)(color >> 8 & 0xff) / 255f;
            b = (float)(color & 0xff) / 255f;
        } else {
            ICBMClassic.logger().error("No valid potion effects found on explosive entity.");
        }

        new BlastPotion(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 20 * scale, 20 * 30, false).setPotionEffect(effect).setRGB(r, g, b).runBlast();
    }
}
