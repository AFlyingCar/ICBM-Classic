package icbm.classic.content.explosive.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.BlockBreakEvent;
import icbm.classic.api.explosion.IMissile;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.explosive.thread.ThreadLargeExplosion;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.TileEntityExplosive;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.Iterator;
import java.util.List;

public class BlastSonic extends Blast
{
    private float energy;
    private boolean hasShockWave = false;

    public BlastSonic(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastSonic(World world, Entity entity, double x, double y, double z, float size, float energy)
    {
        this(world, entity, x, y, z, size);
        this.energy = energy;
    }

    public Blast setShockWave()
    {
        this.hasShockWave = true;
        return this;
    }

    @Override
    public void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            /* TODO re-add?
            if (this.hasShockWave)
            {
                for (int x = (int) (-this.getRadius() * 2); x < this.getRadius() * 2; ++x)
                {
                    for (int y = (int) (-this.getRadius() * 2); y < this.getRadius() * 2; ++y)
                    {
                        for (int z = (int) (-this.getRadius() * 2); z < this.getRadius() * 2; ++z)
                        {
                            Location targetPosition = position.add(new Pos(x, y, z));
                            Block blockID = world().getBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                            if (blockID != Blocks.air)
                            {
                                Material material = blockID.getMaterial();

                                if (blockID != Blocks.bedrock && !(material.isLiquid()) && (blockID.getExplosionResistance(this.exploder, world(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), position.xi(), position.yi(), position.zi()) > this.power || material == Material.glass))
                                {
                                    targetPosition.setBlock(world(), Blocks.air);
                                }
                            }
                        }
                    }
                }
            } */

            createAndStartThread(new ThreadLargeExplosion(this, (int) this.getBlastRadius(), this.energy, this.exploder));
        }

        if (this.hasShockWave)
        {
            ICBMSounds.HYPERSONIC.play(world, location.x(), location.y(), location.z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
        else
        {
            ICBMSounds.SONICWAVE.play(world, location.x(), location.y(), location.z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
    }

    @Override
    public void doExplode() //TODO Rewrite this entire method
    {
        int r = this.callCount;

        if (world() != null && !this.world().isRemote)
        {
            try
            {
                if (isThreadCompleted())
                {
                    if (!getThreadResults().isEmpty())
                    {
                        Iterator<BlockPos> it = getThreadResults().iterator();

                        while (it.hasNext())
                        {
                            BlockPos targetPosition = it.next();
                            double distance = location.distance(targetPosition);

                            // TODO: Add check for if targetPosition can be modified

                            if (distance > r || distance < r - 3)
                            {
                                continue;
                            }

                            final IBlockState blockState = world.getBlockState(targetPosition);
                            final Block block = blockState.getBlock();

                            if (block == Blocks.AIR || blockState.getBlockHardness(world, targetPosition) < 0)
                            {
                                continue;
                            }

                            if (distance < r - 1 || this.world().rand.nextInt(3) > 0)
                            {
                                final int curr_r = r;
                                MinecraftForge.EVENT_BUS.post(new BlockBreakEvent(world, targetPosition,
                                        () -> {
                                            if (block == ICBMClassic.blockExplosive) {
                                                BlockExplosive.triggerExplosive(this.world(), targetPosition, ((TileEntityExplosive) this.world().getTileEntity(targetPosition)).explosive, 1);
                                            } else {
                                                this.world().setBlockToAir(targetPosition);
                                            }

                                            if (this.world().rand.nextFloat() < 0.3 * (this.getBlastRadius() - curr_r)) {
                                                EntityFlyingBlock entity = new EntityFlyingBlock(this.world(), targetPosition, blockState);
                                                this.world().spawnEntity(entity);
                                                entity.yawChange = 50 * this.world().rand.nextFloat();
                                                entity.pitchChange = 100 * this.world().rand.nextFloat();
                                            }
                                        }));

                                it.remove();
                            }
                        }
                    }
                    else
                    {
                        isAlive = false;
                        if(ConfigDebug.DEBUG_THREADS)
                        {
                            String msg = String.format("BlastSonic#doPostExplode() -> Thread failed to find blocks to edit. Either thread failed or no valid blocks were found in range." +
                                            "\nWorld = %s " +
                                            "\nThread = %s" +
                                            "\nSize = %s" +
                                            "\nPos = %s",
                                    world, getThread(), size, location);
                            ICBMClassic.logger().error(msg);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                String msg = String.format("BlastSonic#doPostExplode() ->  Unexpected error while running post detonation code " +
                                "\nWorld = %s " +
                                "\nThread = %s" +
                                "\nSize = %s" +
                                "\nPos = %s",
                        world, getThread(), size, location);
                ICBMClassic.logger().error(msg, e);
            }
        }

        int radius = 2 * this.callCount;
        AxisAlignedBB bounds = new AxisAlignedBB(location.x() - radius, location.y() - radius, location.z() - radius, location.x() + radius, location.y() + radius, location.z() + radius);
        List<Entity> allEntities = this.world().getEntitiesWithinAABB(Entity.class, bounds);

        synchronized (allEntities)
        {
            for (Iterator it = allEntities.iterator(); it.hasNext(); )
            {
                Entity entity = (Entity) it.next();

                if (entity instanceof IMissile) //TODO why?
                {
                    ((IMissile) entity).destroyMissile(true);
                    break; //TODO why stop looping entities?
                }
                else
                {
                    double xDifference = entity.posX - location.x();
                    double zDifference = entity.posZ - location.z();

                    r = (int) this.getBlastRadius();
                    if (xDifference < 0)
                    {
                        r = (int) -this.getBlastRadius();
                    }

                    entity.motionX += (r - xDifference) * 0.02 * this.world().rand.nextFloat();
                    entity.motionY += 3 * this.world().rand.nextFloat();

                    r = (int) this.getBlastRadius();
                    if (zDifference < 0)
                    {
                        r = (int) -this.getBlastRadius();
                    }

                    entity.motionZ += (r - zDifference) * 0.02 * this.world().rand.nextFloat();
                }
            }
        }

        if (this.callCount > this.getBlastRadius())
        {
            this.controller.endExplosion();
        }
    }

    /**
     * The interval in ticks before the next procedural call of this explosive
     *
     * @return - Return -1 if this explosive does not need proceudral calls
     */
    @Override
    public int proceduralInterval()
    {
        return 4;
    }
}
