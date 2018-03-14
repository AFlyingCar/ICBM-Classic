package icbm.classic.content.explosive.handlers.missiles;

import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Antiballistic missile.
 *
 * @author Calclavia
 */
public class MissileAnti extends Missile
{
    public static final int ABMRange = 30;

    public MissileAnti()
    {
        super("antiBallistic", EnumTier.TWO);
        this.hasBlock = false;
    }

    @Override
    public void update(EntityMissile missileObj)
    {
        if (missileObj.lockedTarget != null)
        {
            Pos target = new Pos(missileObj.lockedTarget);

            if (missileObj.lockedTarget.isDead)
            {
                missileObj.doExplosion();
                return;
            }

            if (missileObj.lockedTarget instanceof EntityMissile)
            {
                target = ((EntityMissile) missileObj.lockedTarget).getPredictedPosition(4);
            }

            missileObj.motionX = (target.x() - missileObj.posX) * (0.3F);
            missileObj.motionY = (target.y() - missileObj.posY) * (0.3F);
            missileObj.motionZ = (target.z() - missileObj.posZ) * (0.3F);

            return;
        }

        AxisAlignedBB bounds = new AxisAlignedBB(
                missileObj.posX - ABMRange, missileObj.posY - ABMRange, missileObj.posZ - ABMRange,
                missileObj.posX + ABMRange, missileObj.posY + ABMRange, missileObj.posZ + ABMRange);
        // TODO: Check if this works.
        Entity nearestEntity = missileObj.world.findNearestEntityWithinAABB(EntityMissile.class, bounds, missileObj);

        if (nearestEntity instanceof EntityMissile)
        {
            // Lock target onto missileObj missile
            missileObj.lockedTarget = nearestEntity;
            missileObj.didTargetLockBefore = true;
            ICBMSounds.TARGET_LOCKED.play(missileObj, 5F, 0.9F, true); //TODO move audio settings to constants attached to configs
        }
        else
        {
            missileObj.motionX = missileObj.deltaPathX / missileObj.missileFlightTime;
            missileObj.motionZ = missileObj.deltaPathZ / missileObj.missileFlightTime;

            if (missileObj.didTargetLockBefore == true)
            {
                missileObj.doExplosion();
            }
        }
    }

    @Override
    public boolean isCruise()
    {
        return true;
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastTNT(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6).setDestroyItems().explode();
    }
}