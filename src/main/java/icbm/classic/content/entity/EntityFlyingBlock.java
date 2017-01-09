package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

/** @author Calclavia */
public class EntityFlyingBlock extends Entity implements IEntityAdditionalSpawnData
{
    public Block blockID = null;
    public int metadata = 0;

    public float yawChange = 0;
    public float pitchChange = 0;

    public float gravity = 0.045f;

    public EntityFlyingBlock(World world)
    {
        super(world);
        this.ticksExisted = 0;
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.setSize(1F, 1F);
    }

    public EntityFlyingBlock(World world, IPos3D position, Block blockID, int metadata)
    {
        super(world);
        this.isImmuneToFire = true;
        this.ticksExisted = 0;
        setSize(0.98F, 0.98F);
        yOffset = height / 2.0F;
        setPosition(position.x() + 0.5, position.y(), position.z() + 0.5);
        motionX = 0D;
        motionY = 0D;
        motionZ = 0D;
        this.blockID = blockID;
        this.metadata = metadata;
    }

    public EntityFlyingBlock(World world, Pos position, Block blockID, int metadata, float gravity)
    {
        this(world, position, blockID, metadata);
        this.gravity = gravity;
    }

    @Override
    public String getCommandSenderName()
    {
        return "Flying Block [" + (blockID != null ? blockID.getUnlocalizedName() : "null") + ", " + metadata + ", " + hashCode() + "]";
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        ByteBufUtils.writeUTF8String(data, this.blockID != null ? Block.blockRegistry.getNameForObject(blockID) : "");
        data.writeInt(this.metadata);
        data.writeFloat(this.gravity);
        data.writeFloat(yawChange);
        data.writeFloat(pitchChange);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        String name = ByteBufUtils.readUTF8String(data);
        if(!name.isEmpty())
        {
            blockID = (Block) Block.blockRegistry.getObject(name);
        }
        else
        {
            blockID = null;
        }
        metadata = data.readInt();
        gravity = data.readFloat();
        yawChange = data.readFloat();
        pitchChange = data.readFloat();
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    public void onUpdate()
    {
        if (this.blockID == null)
        {
            this.setDead();
            return;
        }

        if (this.posY > 400 || this.blockID == Engine.multiBlock || this.blockID == Blocks.piston_extension || this.blockID instanceof IFluidBlock)
        {
            this.setDead();
            return;
        }

        this.motionY -= gravity;

        if (this.isCollided)
        {
            this.func_145771_j(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.yawChange > 0)
        {
            this.rotationYaw += this.yawChange;
            this.yawChange -= 2;
        }

        if (this.pitchChange > 0)
        {
            this.rotationPitch += this.pitchChange;
            this.pitchChange -= 2;
        }

        if ((this.onGround && this.ticksExisted > 20) || this.ticksExisted > 20 * 120)
        {
            this.setBlock();
            return;
        }

        this.ticksExisted++;
    }

    public void setBlock()
    {
        if (!this.worldObj.isRemote)
        {
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY);
            int k = MathHelper.floor_double(posZ);

            this.worldObj.setBlock(i, j, k, this.blockID, this.metadata, 2);
        }

        this.setDead();
    }

    /** Checks to see if and entity is touching the missile. If so, blow up! */

    @Override
    public AxisAlignedBB getCollisionBox(Entity par1Entity)
    {
        // Make sure the entity is not an item
        if (par1Entity instanceof EntityLiving)
        {
            if (blockID != null)
            {
                if (!(blockID instanceof IFluidBlock) && (this.motionX > 2 || this.motionY > 2 || this.motionZ > 2))
                {
                    int damage = (int) (1.2 * (Math.abs(this.motionX) + Math.abs(this.motionY) + Math.abs(this.motionZ)));
                    ((EntityLiving) par1Entity).attackEntityFrom(DamageSource.fallingBlock, damage);
                }
            }
        }

        return null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setInteger("metadata", this.metadata);
        if(blockID != null)
        {
            nbttagcompound.setString("block", Block.blockRegistry.getNameForObject(blockID));
        }
        nbttagcompound.setFloat("gravity", this.gravity);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        this.metadata = nbttagcompound.getInteger("metadata");
        if(nbttagcompound.hasKey("blockID"))
        {
            this.blockID = (Block) Block.blockRegistry.getObject(nbttagcompound.getString("block"));
        }
        this.gravity = nbttagcompound.getFloat("gravity");
    }

    @Override
    public float getShadowSize()
    {
        return 0.5F;
    }

    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }
}