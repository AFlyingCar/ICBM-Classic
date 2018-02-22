package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/main")
@Config.LangKey("config.icbmclassic:main.title")
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class ConfigMain
{
    @Config.Name("use_energy")
    @Config.Comment("Range of tier 1 launcher")
    public static boolean REQUIRES_POWER = true;

    @Config.Name("handheld_launcher_tier_limit")
    @Config.Comment("Limits the max tier the handheld launcher can fire,} outside of creative mode")
    @Config.RangeInt(min = 1, max = 4)
    public static int ROCKET_LAUNCHER_TIER_FIRE_LIMIT = 2;

    @SubscribeEvent
    public static void onConfigChangedEvent(final ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(ICBMClassic.DOMAIN))
        {
            ConfigManager.sync(ICBMClassic.DOMAIN, Config.Type.INSTANCE);
        }
    }
}
