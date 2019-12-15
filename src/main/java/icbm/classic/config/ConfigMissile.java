package icbm.classic.config;

import icbm.classic.ICBMClassic;
import icbm.classic.content.missile.EntityMissile;
import net.minecraftforge.common.config.Config;

/**
 * Configs for {@link EntityMissile}
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2018.
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/missile")
@Config.LangKey("config.icbmclassic:missile.title")
public class ConfigMissile
{
    @Config.Name("speed")
    @Config.Comment("Speed limiter of the missile when moving upwards out of the launcher")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public static float LAUNCH_SPEED = 0.012F;


    @Config.Name("nuclear_cluster_size")
    @Config.Comment("Number of missiles to spawn for nuclear cluster missile")
    @Config.RangeInt(min = 1, max = 30)
    public static int NUCLEAR_CLUSTER_SIZE = 4;

    @Config.Name("cluster_size")
    @Config.Comment("Number of missiles to spawn for cluster missile")
    @Config.RangeInt(min = 1, max = 30)
    public static int CLUSTER_SIZE = 12;

    @Config.Name("simulation_start_height")
    @Config.Comment("Height (y level) to start simulating a missile when it travels above the map")
    @Config.RangeInt(min = 1)
    public static int SIMULATION_START_HEIGHT = 300;

    @Config.Name("missile_chunkload_radius")
    @Config.Comment("Radius around a missile that will be chunkloaded")
    @Config.RangeInt(min=1, max=6)
    public static int MISSILE_CHUNKLOAD_RADIUS = 3;
}
