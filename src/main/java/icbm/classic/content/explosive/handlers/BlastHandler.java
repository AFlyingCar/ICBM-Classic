package icbm.classic.content.explosive.handlers;

import icbm.classic.api.events.BlockBreakEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class BlastHandler
{
    private static Predicate<Object> callback = null;

    public static void setCallback(Predicate<Object> _callback) { callback = _callback; }

    @SubscribeEvent
    public static void onBlockBreak(BlockBreakEvent event)
    {
        // If we have a callback, and that callback says not to continue, then don't bother doing anything
        if(callback != null)
        {
            if(!callback.test(event))
            {
                return;
            }
        }

        switch(event.getBreakageType())
        {
            case SET_TO_AIR:
                // System.out.println("Set To Air");
                event.getWorld().setBlockToAir(event.getPosition());
                break;
            case SET_STATE:
                // System.out.println("Set State");
                event.getWorld().setBlockState(event.getPosition(), event.getNewState());
                break;
            case SET_STATE_WITH_FLAGS:
                // System.out.println("Set State with flags");
                event.getWorld().setBlockState(event.getPosition(), event.getNewState(), event.getFlags());
                break;
            case USE_CALLBACK:
                // System.out.println("With custom callback");
                event.getCallback().run();
                break;
        }
    }
}
