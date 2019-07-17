package icbm.classic.content.machines.radarstation;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.content.missile.MissileFlightType;
import icbm.classic.lib.IGuiTile;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Point;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.TileFrequency;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.HashMap;

import net.minecraftforge.fml.common.Optional;

import li.cil.oc.api.network.SimpleComponent;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers", striprefs = true)
public class TileRadarStation extends TileFrequency implements IPacketIDReceiver, IRadioWaveSender, IGuiTile, IInventoryProvider<ExternalInventory>, SimpleComponent
{
    /** Max range the radar station will attempt to find targets inside */
    public final static int MAX_DETECTION_RANGE = 500;

    public final static int GUI_PACKET_ID = 1;

    public float rotation = 0;
    public int alarmRange = 100;
    public int safetyRange = 50;

    public boolean emitAll = true;

    public List<Entity> detectedEntities = new ArrayList<Entity>();
    /** List of all incoming missiles, in order of distance. */
    private List<EntityMissile> incomingMissiles = new ArrayList<EntityMissile>();

    ExternalInventory inventory;

    protected List<Pos> guiDrawPoints = new ArrayList();
    protected RadarObjectType[] types;
    protected boolean updateDrawList = true;

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
            //Update client every 1 seconds
            if (this.ticks % 20 == 0)
            {
                sendDescPacket();
            }

            //If we have energy
            if (checkExtract())
            {
                //Remove energy
                //this.extractEnergy(); TODO fix so only removes upkeep cost

                // Do a radar scan
                if (ticks % 3 == 0) //TODO make config to control scan rate to reduce lag
                {
                    this.doScan(); //TODO consider rewriting to not cache targets
                }

                //Check for incoming and launch anti-missiles if
                if (this.ticks % 20 == 0 && this.incomingMissiles.size() > 0) //TODO track if a anti-missile is already in air to hit target
                {
                    RadioRegistry.popMessage(world, this, getFrequency(), "fireAntiMissile", this.incomingMissiles.get(0));
                }
            }
            else
            {
                if (detectedEntities.size() > 0)
                {
                    world.setBlockState(getPos(), getBlockState().withProperty(BlockRadarStation.REDSTONE_PROPERTY, false));
                }

                incomingMissiles.clear();
                detectedEntities.clear();
            }

            //Update redstone state
            final boolean shouldBeOn = checkExtract() && detectedEntities.size() > 0;
            if (world.getBlockState(getPos()).getValue(BlockRadarStation.REDSTONE_PROPERTY) != shouldBeOn)
            {
                world.setBlockState(getPos(), getBlockState().withProperty(BlockRadarStation.REDSTONE_PROPERTY, shouldBeOn));
                for (EnumFacing facing : EnumFacing.HORIZONTALS)
                {
                    BlockPos pos = getPos().add(facing.getXOffset(), facing.getYOffset(), facing.getZOffset());
                    for (EnumFacing enumfacing : EnumFacing.values())
                    {
                        world.notifyNeighborsOfStateChange(pos.offset(enumfacing), getBlockType(), false);
                    }
                }
            }
        }
        else
        {
            if (checkExtract()) //TODO use a boolean on client for on/off state
            {
                if (updateDrawList)
                {
                    guiDrawPoints.clear();
                    for (int i = 0; i < detectedEntities.size(); i++)
                    {
                        Entity entity = detectedEntities.get(i);
                        if (entity != null)
                        {
                            guiDrawPoints.add(new Pos(entity.posX, entity.posZ, types[i].ordinal()));
                        }
                    }
                }

                //Animation
                this.rotation += 0.08f;
                if (this.rotation > 360)
                {
                    this.rotation = 0;
                }
            }
            else
            {
                guiDrawPoints.clear();
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return false; //Don't kill tile
    }

    private void doScan() //TODO document and thread
    {
        this.incomingMissiles.clear();
        this.detectedEntities.clear();

        List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world, xi() + 1.5, yi() + 0.5, zi() + 0.5, Math.min(alarmRange, MAX_DETECTION_RANGE));

        for (Entity entity : entities)
        {
            if (entity instanceof EntityMissile && ((EntityMissile) entity).getExplosiveType() != Explosives.MISSILE_ANTI.handler)
            {
                if (((EntityMissile) entity).getTicksInAir() > -1)
                {
                    if (!this.detectedEntities.contains(entity))
                    {
                        this.detectedEntities.add(entity);
                    }

                    if (this.isMissileGoingToHit((EntityMissile) entity))
                    {
                        if (this.incomingMissiles.size() > 0)
                        {
                            /** Sort in order of distance */
                            double dist = new Pos((TileEntity) this).distance(new Pos(entity));

                            for (int i = 0; i < this.incomingMissiles.size(); i++)
                            {
                                EntityMissile missile = this.incomingMissiles.get(i);

                                if (dist < new Pos((TileEntity) this).distance((IPos3D) missile))
                                {
                                    this.incomingMissiles.add(i, (EntityMissile) entity);
                                    break;
                                }
                                else if (i == this.incomingMissiles.size() - 1)
                                {
                                    this.incomingMissiles.add((EntityMissile) entity);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            this.incomingMissiles.add((EntityMissile) entity);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks to see if the missile will hit within the range of the radar station
     *
     * @param missile - missile being checked
     * @return true if it will
     */
    public boolean isMissileGoingToHit(EntityMissile missile)
    {
        if (missile == null)
        {
            return false;
        }


        if (missile.targetPos == null)
        {
            Vec3d mpos = new Vec3d(missile.xf(),missile.yf(), missile.zf());    // missile position
            Vec3d rpos = new Vec3d(this.pos.getX(),this.pos.getY(), this.pos.getZ());   // radar position

            double nextDistance = mpos.add(missile.getVelocity().toVec3d()).distanceTo(rpos);   // next distance from missile to radar
            double currentDistance = mpos.distanceTo(rpos); // current distance from missile to radar

            return nextDistance < currentDistance;   // we assume that the missile hits if the distance decreases (the missile is coming closer)
        }

        double d = missile.targetPos.distance(this);
        //TODO simplify code to not use vector system
        return d < this.safetyRange;
    }

    @Override
    protected PacketTile getGUIPacket()
    {
        PacketTile packet = new PacketTile("gui", GUI_PACKET_ID, this);
        packet.write(alarmRange);
        packet.write(safetyRange);
        packet.write(getFrequency());
        packet.write(detectedEntities.size());
        if (detectedEntities.size() > 0)
        {
            for (Entity entity : detectedEntities)
            {
                if (entity != null && entity.isEntityAlive())
                {
                    packet.write(entity.getEntityId());

                    int type = RadarObjectType.OTHER.ordinal();
                    if (entity instanceof EntityMissile)
                    {
                        type = isMissileGoingToHit((EntityMissile) entity) ? RadarObjectType.MISSILE_IMPACT.ordinal() : RadarObjectType.MISSILE.ordinal();
                    }
                    packet.write(type);
                }
                else
                {
                    packet.write(-1);
                    packet.write(0);
                }
            }
        }
        return packet;
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        setEnergy(buf.readInt());
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(getEnergy());
    }

    @Override
    public boolean read(ByteBuf data, int ID, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, ID, player, type))
        {
            if (this.world.isRemote)
            {
                if (ID == GUI_PACKET_ID)
                {
                    this.alarmRange = data.readInt();
                    this.safetyRange = data.readInt();
                    this.setFrequency(data.readInt());

                    this.updateDrawList = true;

                    types = null;
                    detectedEntities.clear(); //TODO recode so we are not getting entities client side

                    int entityListSize = data.readInt();
                    types = new RadarObjectType[entityListSize];

                    for (int i = 0; i < entityListSize; i++)
                    {
                        int id = data.readInt();
                        if (id != -1)
                        {
                            Entity entity = world.getEntityByID(id);
                            if (entity != null)
                            {
                                detectedEntities.add(entity);
                            }
                        }
                        types[i] = RadarObjectType.get(data.readInt());
                    }
                    return true;
                }
            }
            else if (!this.world.isRemote)
            {
                if (ID == 2)
                {
                    this.safetyRange = data.readInt();
                    return true;
                }
                else if (ID == 3)
                {
                    this.alarmRange = data.readInt();
                    return true;
                }
                else if (ID == 4)
                {
                    this.setFrequency(data.readInt());
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public int getStrongRedstonePower(EnumFacing side)
    {
        if (incomingMissiles.size() > 0)
        {
            if (this.emitAll)
            {
                return Math.min(15, 5 + incomingMissiles.size());
            }

            for (EntityMissile incomingMissile : this.incomingMissiles)
            {
                Point position = new Point(incomingMissile.x(), incomingMissile.y());
                EnumFacing missileTravelDirection = EnumFacing.DOWN;
                double closest = -1;

                for (EnumFacing rotation : EnumFacing.HORIZONTALS)
                {
                    double dist = position.distance(new Point(this.getPos().getX() + rotation.getXOffset(), this.getPos().getZ() + rotation.getZOffset()));

                    if (dist < closest || closest < 0)
                    {
                        missileTravelDirection = rotation;
                        closest = dist;
                    }
                }

                if (missileTravelDirection.getOpposite().ordinal() == side.ordinal())
                {
                    return Math.min(15, 5 + incomingMissiles.size());
                }
            }
        }

        return 0;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.safetyRange = nbt.getInteger("safetyBanJing");
        this.alarmRange = nbt.getInteger("alarmBanJing");
        this.emitAll = nbt.getBoolean("emitAll");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("safetyBanJing", this.safetyRange);
        nbt.setInteger("alarmBanJing", this.alarmRange);
        nbt.setBoolean("emitAll", this.emitAll);
        return super.writeToNBT(nbt);
    }

    @Override
    public void sendRadioMessage(float hz, String header, Object... data)
    {
        RadioRegistry.popMessage(world, this, hz, header, data);
    }

    @Override
    public Cube getRadioSenderRange()
    {
        return null;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerRadarStation(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiRadarStation(player, this);
    }

    // ------------------------------------------------------------------------
    // OpenComputers Integration Methods

    private void fillEntityMapping(Map<String, Object> m, Entity e) {
        m.put("chunkCoordX", e.chunkCoordX);
        m.put("chunkCoordY", e.chunkCoordY);
        m.put("chunkCoordZ", e.chunkCoordZ);

        m.put("dimension", e.dimension);
        m.put("height", e.height);
        m.put("isAirBorne", e.isAirBorne);

        m.put("motionX", e.motionX);
        m.put("motionY", e.motionY);
        m.put("motionZ", e.motionZ);

        m.put("posX", e.posX);
        m.put("posY", e.posY);
        m.put("posZ", e.posZ);

        m.put("distance", e.getDistance(getPos().getX(), getPos().getY(), getPos().getZ()));

        m.put("isBeingRidden", e.isBeingRidden());
    }

    private Map<String, Object> posToMap(Pos p) {
        Map<String, Object> m = new HashMap<String, Object>();

        m.put("x", p.xi());
        m.put("y", p.yi());
        m.put("z", p.zi());

        return m;
    }

    private Pos mapToPos(Map<String, Object> m) {
        return new Pos((double)m.get("x"), (double)m.get("y"), (double)m.get("z"));
    }

    @Override
    public String getComponentName() {
        return "radar_station";
    }

    @Callback(doc = "function():number -- Gets the maximum range that a missile can be in order to be detected.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getAlarmRange(Context c, Arguments a) {
        return new Object[] { new Integer(alarmRange) };
    }

    @Callback(doc = "function(number):boolean -- Sets the maximum range that a missile can be in order to be detected.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setAlarmRange(Context c, Arguments a) {
        alarmRange = a.checkInteger(0);

        return new Object[] { new Boolean(true) };
    }

    @Callback(doc = "function(number):boolean -- Gets the maximum range that a missile can be before an redstone signal is fired.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getSafetyRange(Context c, Arguments a) {
        return new Object[] { new Integer(safetyRange) };
    }

    @Callback(doc = "function(number):boolean -- Sets the maximum range that a missile can be before an redstone signal is fired.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setSafetyRange(Context c, Arguments a) {
        safetyRange = a.checkInteger(0);

        return new Object[] { new Boolean(true) };
    }

    @Callback(doc = "function():table -- Gets the position of the radar station in the world.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getPos(Context c, Arguments a) {
        return new Object[] { posToMap(new Pos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) };
    }

    @Callback(doc = "function():table -- Returns all entities detected by this radar station.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getDetectedEntities(Context c, Arguments a) {
        Map<Integer, Map<String, Object>> m = new HashMap<Integer, Map<String, Object>>();

        for(int i = 0; i < detectedEntities.size(); ++i) {
            m.put(i, new HashMap<String, Object>());

            m.get(i).put("__ptr", detectedEntities.get(i));

            fillEntityMapping(m.get(i), detectedEntities.get(i));
        }

        return new Object[] { m };
    }

    @Callback(doc = "function():table -- Returns all incoming missiles, in order of distance.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getIncomingMissiles(Context c, Arguments a) {
        Map<Integer, Map<String, Object>> m = new HashMap<Integer, Map<String, Object>>();

        for(int i = 0; i < incomingMissiles.size(); ++i) {
            m.put(i, new HashMap<String, Object>());

            m.get(i).put("__ptr", incomingMissiles.get(i));

            fillEntityMapping(m.get(i), incomingMissiles.get(i));

            m.get(i).put("velocity", posToMap(incomingMissiles.get(i).getVelocity()));
            m.get(i).put("explosiveID", incomingMissiles.get(i).explosiveID);
            m.get(i).put("maxHeight", incomingMissiles.get(i).maxHeight);
            m.get(i).put("targetPos", posToMap(incomingMissiles.get(i).targetPos));
            m.get(i).put("launcherPos", posToMap(incomingMissiles.get(i).launcherPos));
            m.get(i).put("acceleration", incomingMissiles.get(i).acceleration);
        }

        return new Object[] { m };
    }

    @Callback(doc = "function(table):boolean -- Returns true if the given missile is going to hit")
    @Optional.Method(modid = "opencomputers")
    public Object[] isMissileGoingToHit(Context c, Arguments a) {
        // NOTE: Because we can't just give Lua a EntityMissile and expect to get
        //   it back, we just repeat the code listed there in the real isMissileGoingToHit method
        Map<String, Object> missile = a.checkTable(0);

        if (missile.get("targetPos") == null)
        {
            Vec3d mpos = new Vec3d((double)missile.get("posX"), (double)missile.get("posY"), (double)missile.get("posZ"));    // missile position
            Vec3d rpos = new Vec3d(this.pos.getX(),this.pos.getY(), this.pos.getZ());   // radar position

            double nextDistance = mpos.add(mapToPos((Map<String, Object>)missile.get("velocity")).toVec3d()).distanceTo(rpos);   // next distance from missile to radar
            double currentDistance = mpos.distanceTo(rpos); // current distance from missile to radar

            // we assume that the missile hits if the distance decreases (the missile is coming closer)
            return new Object[] { new Boolean(nextDistance < currentDistance) };
        }

        double d = mapToPos((Map<String, Object>)missile.get("targetPos")).distance(this);
        //TODO simplify code to not use vector system
        return new Object[] { new Boolean(d < this.safetyRange) };

    }
}
