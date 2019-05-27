package icbm.classic.content.explosive.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.BlockBreakEvent;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.explosive.thread.ThreadSmallExplosion;
import icbm.classic.lib.PosDistanceSorter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

public class BlastAntiGravitational extends Blast
{
    protected ThreadSmallExplosion thread;
    protected Set<EntityFlyingBlock> flyingBlocks = new HashSet<EntityFlyingBlock>();

    public BlastAntiGravitational(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            this.thread = new ThreadSmallExplosion(this, (int) this.getBlastRadius(), this.exploder);
            this.thread.start();
        }

        //this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), References.PREFIX + "antigravity", 6.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
    }

    @Override
    public void doExplode() //TODO rewrite entire method
    {
        int r = this.callCount;

        if(world() != null && !this.world().isRemote)
        {
            try
            {
                if (this.thread != null) //TODO replace thread check with callback triggered by thread and delayed into main thread
                {
                    if (this.thread.isComplete)
                    {
                        //Copy as concurrent list is not fast to sort
                        List<BlockPos> results = new ArrayList();
                        results.addAll(getThreadResults());

                        if (r == 0)
                        {
                            Collections.sort(results, new PosDistanceSorter(location, true));
                        }
                        int blocksToTake = 20;

                        for (BlockPos targetPosition : results)
                        {

                            // TODO: Add check for if we can modify the block at targetPosition

                            final IBlockState blockState = world.getBlockState(targetPosition);
                            if (blockState.getBlock() != Blocks.AIR)
                            {
                                float hardness = blockState.getBlockHardness(world, targetPosition);
                                if (hardness >= 0 && hardness < 1000)
                                {
                                    if (world().rand.nextInt(3) > 0)
                                    {
                                    //Mark blocks taken
                                    blocksToTake--;
                                    if (blocksToTake <= 0)
                                    {
                                        break;
                                    }
                                    MinecraftForge.EVENT_BUS.post(new BlockBreakEvent(world, targetPosition,
                                            () -> {
                                                //Remove block
                                                world.setBlockToAir(targetPosition);


                                                //Create flying block
                                                EntityFlyingBlock entity = new EntityFlyingBlock(world(), targetPosition, blockState, 0);
                                                entity.yawChange = 50 * world().rand.nextFloat();
                                                entity.pitchChange = 100 * world().rand.nextFloat();
                                                entity.motionY += Math.max(0.15 * world().rand.nextFloat(), 0.1);
                                                entity.noClip = true;
                                                world().spawnEntity(entity);

                                                //Track flying block
                                                flyingBlocks.add(entity);
                                            } ));
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    String msg = String.format("BlastAntiGravitational#doPostExplode() -> Failed to run due to null thread" +
                                    "\nWorld = %s " +
                                    "\nThread = %s" +
                                    "\nSize = %s" +
                                    "\nPos = ",
                            world, thread, size, location);
                    ICBMClassic.logger().error(msg);
                }
            }
            catch (Exception e)
            {
                String msg = String.format("BlastAntiGravitational#doPostExplode() ->  Unexpected error while running post detonation code " +
                                "\nWorld = %s " +
                                "\nThread = %s" +
                                "\nSize = %s" +
                                "\nPos = ",
                        world, thread, size, location);
                ICBMClassic.logger().error(msg, e);
            }
        }

        int radius = (int) this.getBlastRadius();
        AxisAlignedBB bounds = new AxisAlignedBB(location.x() - radius, location.y() - radius, location.z() - radius, location.y() + radius, 100, location.z() + radius);
        List<Entity> allEntities = world().getEntitiesWithinAABB(Entity.class, bounds);

        for (Entity entity : allEntities)
        {
            if (!(entity instanceof EntityFlyingBlock) && entity.posY < 100 + location.y())
            {
                if (entity.motionY < 0.4)
                {
                    entity.motionY += 0.15;
                }
            }
        }

        if (this.callCount > 20 * 120)
        {
            this.controller.endExplosion();
        }
    }

    @Override
    protected void doPostExplode()
    {
        for (EntityFlyingBlock entity : flyingBlocks)
        {
            entity.gravity = 0.045f;
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
        return 1;
    }

    @Override
    public float getBlastRadius()
    {
        return 15;
    }

}
