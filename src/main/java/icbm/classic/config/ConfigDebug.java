package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/10/2018.
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/debug")
@Config.LangKey("config.icbmclassic:debug.title")
public class ConfigDebug
{
    @Config.Name("debug_threads")
    @Config.Comment("Enables extra console output to check the state of the explosive threads.")
    public static boolean DEBUG_THREADS = false;

    @Config.Name("debug_explosives")
    @Config.Comment("Enables extra console output to check the state of the explosive logic.")
    public static boolean DEBUG_EXPLOSIVES = false;

    @Config.Name("debug_missile_launches")
    @Config.Comment("When enabled, all missile launches are logged, including target and origin coordinates.")
    public static boolean DEBUG_MISSILE_LAUNCHES = false;

    @Config.Name("debug_missile_tracker")
    @Config.Comment("When enabled, additional debug output is written to the console regarding the missile tracker.")
    public static boolean DEBUG_MISSILE_TRACKER = false;
}
