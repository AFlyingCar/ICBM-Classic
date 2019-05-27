package icbm.classic.api.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.function.Function;

public class BlockBreakEvent extends Event
{
    private World world;
    private BlockPos position;
    private IBlockState newState;
    private int flags;
    private Runnable callback;

    public enum BlockBreakType
    {
        SET_TO_AIR, SET_STATE, SET_STATE_WITH_FLAGS, USE_CALLBACK
    }

    private BlockBreakType breakageType;

    public BlockBreakEvent(World _world, BlockPos _position)
    {
        world = _world;
        position = _position;
        newState = null;
        flags = 0;
        breakageType = BlockBreakType.SET_TO_AIR;
    }

    public BlockBreakEvent(World _world, BlockPos _position,  IBlockState _newState)
    {
        world = _world;
        position = _position;
        newState = _newState;
        breakageType = BlockBreakType.SET_STATE;
    }

    public BlockBreakEvent(World _world, BlockPos _position,  IBlockState _newState, int _flags)
    {
        world = _world;
        position = _position;
        newState = _newState;
        flags = _flags;
        breakageType = BlockBreakType.SET_STATE_WITH_FLAGS;
    }

    public BlockBreakEvent(World _world, BlockPos _position, Runnable _callback)
    {
        breakageType = BlockBreakType.USE_CALLBACK;
        callback = _callback;
        world = _world;
        position = _position;
    }

    public BlockBreakType getBreakageType() {
        return breakageType;
    }

    public BlockPos getPosition() {
        return position;
    }

    public IBlockState getNewState() {
        return newState;
    }

    public int getFlags() {
        return flags;
    }

    public World getWorld() {
        return world;
    }

    public Runnable getCallback() {
        return callback;
    }
}
