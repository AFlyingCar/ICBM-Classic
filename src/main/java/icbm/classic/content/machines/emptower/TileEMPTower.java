package icbm.classic.content.machines.emptower;

import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.explosive.blast.BlastEMP;
import icbm.classic.content.multiblock.MultiBlockHelper;
import icbm.classic.lib.IGuiTile;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.item.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Optional;

import li.cil.oc.api.network.SimpleComponent;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers", striprefs = true)
public class TileEMPTower extends TilePoweredMachine implements IMultiTileHost, IPacketIDReceiver, IGuiTile, IInventoryProvider<ExternalInventory>, SimpleComponent
{
    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150;

    public static List<BlockPos> tileMapCache = new ArrayList();

    static
    {
        tileMapCache.add(new BlockPos(0, 1, 0));
    }

    public float rotation = 0;
    private float rotationDelta;

    // The EMP mode. 0 = All, 1 = Missiles Only, 2 = Electricity Only
    public byte empMode = 0; //TODO move to enum

    private int cooldownTicks = 0;

    // The EMP explosion radius
    public int empRadius = 60;

    private boolean _destroyingStructure = false;

    private ExternalInventory inventory;

    @Override
    public ExternalInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(this, 2);
        }
        return inventory;
    }

    @Override
    public void update()
    {
        super.update();
        if (isServer())
        {
            if (!isReady())
            {
                cooldownTicks--;
            }
            else
            {
                if (ticks % 20 == 0 && getEnergy() > 0)
                {
                    ICBMSounds.MACHINE_HUM.play(world, xi(), yi(), zi(), 0.5F, 0.85F * getChargePercentage(), true);
                    sendDescPacket();
                }
                if (world.getRedstonePowerFromNeighbors(getPos()) > 0)
                {
                    fire();
                }
            }
        }
        else
        {
            rotationDelta = (float) (Math.pow(getChargePercentage(), 2) * 0.5);
            rotation += rotationDelta;
            if (rotation > 360)
            {
                rotation = 0;
            }
        }
    }

    public float getChargePercentage()
    {
        return Math.min(1, getEnergy() / (float) getEnergyConsumption());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, id, player, type))
        {
            switch (id)
            {
                case 1: //TODO constant
                {
                    empRadius = data.readInt();
                    return true;
                }
                case 2://TODO constant
                {
                    empMode = data.readByte();
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(empRadius);
        buf.writeByte(empMode);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        empRadius = buf.readInt();
        empMode = buf.readByte();
    }

    @Override
    public int getEnergyBufferSize()
    {
        return Math.max(3000000 * (this.empRadius / MAX_RADIUS), 1000000);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        this.empRadius = par1NBTTagCompound.getInteger("empRadius");
        this.empMode = par1NBTTagCompound.getByte("empMode");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setInteger("empRadius", this.empRadius);
        par1NBTTagCompound.setByte("empMode", this.empMode);
        return super.writeToNBT(par1NBTTagCompound);
    }

    //@Callback(limit = 1)
    public boolean fire()
    {
        if (this.checkExtract())
        {
            if (isReady())
            {
                switch (this.empMode)
                {
                    default:
                        new BlastEMP(world, null, this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5, this.empRadius).setEffectBlocks().setEffectEntities().runBlast();
                        break;
                    case 1:
                        new BlastEMP(world, null, this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5, this.empRadius).setEffectEntities().runBlast();
                        break;
                    case 2:
                        new BlastEMP(world, null, this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5, this.empRadius).setEffectBlocks().runBlast();
                        break;
                }
                this.extractEnergy();
                this.cooldownTicks = getMaxCooldown();
                return true;
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback
    public boolean isReady()
    {
        return getCooldown() <= 0;
    }

    //@Callback
    public int getCooldown()
    {
        return cooldownTicks;
    }

    //@Callback
    public int getMaxCooldown()
    {
        return 120;
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                tileMulti.setHost(this);
            }
        }
    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        if (!_destroyingStructure && tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, EnumHand hand, EnumFacing side, float xHit, float yHit, float zHit)
    {
        if (isServer())
        {
            openGui(player, 0);
        }
        return true;
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public List<BlockPos> getLayoutOfMultiBlock()
    {
        return tileMapCache;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerEMPTower(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiEMPTower(player, this);
    }

    // ------------------------------------------------------------------------
    // OpenComputers Integration Methods

    @Override
    public String getComponentName() {
        return "emp_tower";
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
    public Object[] greet(Context ctx, Arguments args) {
        return new Object[] { "Hello, I am an EMP tower!" };
    }

    @Callback(doc = "function():boolean -- Fires the EMP Tower and returns true if it fired successfully.")
    @Optional.Method(modid = "opencomputers")
    public Object[] fire(Context ctx, Arguments args) {
        return new Object[] { new Boolean(fire()) };
    }

    @Callback(doc = "function():boolean -- Returns true if the EMP Tower is ready to fire.")
    @Optional.Method(modid = "opencomputers")
    public Object[] isReady(Context ctx, Arguments args)
    {
        return new Object[] { new Boolean(isReady()) };
    }

    @Callback(doc = "function():number -- Returns the amount of time until the EMP Tower can fire again.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getCooldown(Context ctx, Arguments args)
    {
        return new Object[] { new Integer(getCooldown()) };
    }

    @Callback(doc = "function():number -- Returns the maximum amount of time that cooldown can take.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getMaxCooldown(Context ctx, Arguments args)
    {
        return new Object[] { new Integer(getMaxCooldown()) };
    }

    @Callback(doc = "function():number -- Returns the current EMP mode.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getEMPMode(Context ctx, Arguments args) {
        return new Object[] { new Integer(empMode) };
    }

    @Callback(doc = "function(number):boolean -- Sets the EMP mode. Must be 1 (Missiles), 2 (Electricity), or 0 (Both)")
    @Optional.Method(modid = "opencomputers")
    public Object[] setEMPMode(Context ctx, Arguments args) {
        int mode = args.checkInteger(0);

        if(mode < 0 || mode > 2) {
            return new Object[]{ false, "Mode must be 0, 1, or 2" };
        }

        empMode = (byte)mode;

        return new Object[] { new Boolean(true) };
    }

    @Callback(doc = "function():number -- Gets the current EMP radius.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getEMPRadius(Context ctx, Arguments args) {
        return new Object[] { new Integer(empRadius) };
    }

    @Callback(doc = "function(number):boolean -- Sets the EMP radius.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setEMPRadius(Context ctx, Arguments args) {
        empRadius = args.checkInteger(0);

        return new Object[] { new Boolean(true) };
    }

    @Callback(doc = "function():number -- Returns how charged the EMP tower is.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getChargePercentage(Context ctx, Arguments args) {
        return new Object[] { new Float(getChargePercentage()) };
    }

    @Callback(doc = "function():number -- Returns the size of the energy buffer for the EMP tower.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyBufferSize(Context ctx, Arguments args) {
        return new Object[] { new Integer(getEnergyBufferSize()) };
    }
}
