package icbm.classic.content.explosive.handlers;

import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityBombCart;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.explosive.blast.BlastPotion;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExPotion extends Explosion {
    public ExPotion(String mingZi, EnumTier tier) {
        super(mingZi, tier);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale) {
        PotionEffect effect = null;
        if(entity instanceof EntityExplosive) {
            EntityExplosive explosive = (EntityExplosive)entity;

            if(explosive.nbtData.hasKey("effect"))
                effect = PotionEffect.readCustomPotionEffectFromNBT(explosive.nbtData.getCompoundTag("effect"));
        } else if(entity instanceof EntityMissile) {
            EntityMissile missile = (EntityMissile)entity;

            effect = missile.getEffect();
        } else if(entity instanceof EntityGrenade) {
            EntityGrenade grenade = (EntityGrenade)entity;

            if(grenade.nbtData.hasKey("effect"))
                effect = PotionEffect.readCustomPotionEffectFromNBT(grenade.nbtData.getCompoundTag("effect"));
        } else if(entity instanceof EntityBombCart) {
            // TODO: We need a special EntityBombCart for potions
            /*
            EntityBombCart bombCart = (EntityBombCart)entity;

            if(bombCart.nbtData.hasKey("effect"))
                effect = PotionEffect.readCustomPotionEffectFromNBT(bombCart.nbtData.getCompoundTag("effect"));
             */
        } else {
            ICBMClassic.logger().error("Entity does not match any known ICBM explosive device!");
        }

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
