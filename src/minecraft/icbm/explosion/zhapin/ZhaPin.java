package icbm.explosion.zhapin;

import icbm.api.ICBM;
import icbm.api.explosion.ExplosionEvent.PostExplosionEvent;
import icbm.api.explosion.ExplosionEvent.PreExplosionEvent;
import icbm.api.explosion.IExplosive;
import icbm.explosion.ZhuYaoExplosion;
import icbm.explosion.daodan.DaoDan;
import icbm.explosion.daodan.EDaoDan;
import icbm.explosion.zhapin.ex.ExBianZhong;
import icbm.explosion.zhapin.ex.ExBingDan;
import icbm.explosion.zhapin.ex.ExBingDan2;
import icbm.explosion.zhapin.ex.ExChaoShengBuo;
import icbm.explosion.zhapin.ex.ExDianCi;
import icbm.explosion.zhapin.ex.ExDianCiSignal;
import icbm.explosion.zhapin.ex.ExDianCiWave;
import icbm.explosion.zhapin.ex.ExFanWuSu;
import icbm.explosion.zhapin.ex.ExFuLan;
import icbm.explosion.zhapin.ex.ExHongSu;
import icbm.explosion.zhapin.ex.ExHuanYuan;
import icbm.explosion.zhapin.ex.ExHuo;
import icbm.explosion.zhapin.ex.ExPiaoFu;
import icbm.explosion.zhapin.ex.ExQunDan;
import icbm.explosion.zhapin.ex.ExShengBuo;
import icbm.explosion.zhapin.ex.ExTaiYang;
import icbm.explosion.zhapin.ex.ExTaiYang2;
import icbm.explosion.zhapin.ex.ExTuPuo;
import icbm.explosion.zhapin.ex.ExTuiLa;
import icbm.explosion.zhapin.ex.ExWan;
import icbm.explosion.zhapin.ex.ExWenYa;
import icbm.explosion.zhapin.ex.ExYaSuo;
import icbm.explosion.zhapin.ex.ExYuanZi;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.TranslationHelper;
import universalelectricity.prefab.implement.ITier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ZhaPin implements ITier, IExplosive
{
	public enum ZhaPinType
	{
		QUAN_BU, ZHA_DAN, SHOU_LIU_DAN, DAO_DAN;

		public static ZhaPinType get(int id)
		{
			if (id >= 0 && id < ZhaPinType.values().length)
			{
				return ZhaPinType.values()[id];
			}

			return null;
		}
	}

	public static final ZhaPin yaSuo = new ExYaSuo("condensed", 0, 1);
	public static final ZhaPin xiaoQunDan = new ExQunDan("shrapnel", 1, 1);
	public static final ZhaPin huo = new ExHuo("incendiary", 2, 1);
	public static final ZhaPin duQi = new ExDuQi("chemical", 3, 1);
	public static final ZhaPin zhen = new ExQunDan("anvil", 4, 1);
	public static final ZhaPin tui = new ExTuiLa("repulsive", 5, 1);
	public static final ZhaPin la = new ExTuiLa("attractive", 6, 1);

	public static final int E_YI_ID = 7;

	public static final ZhaPin qunDan = new ExQunDan("fragmentation", 7, 2);
	public static final ZhaPin chuanRan = new ExDuQi("contagious", 8, 2);
	public static final ZhaPin shengBuo = new ExShengBuo("sonic", 9, 2);
	public static final ZhaPin tuPuo = new ExTuPuo("breaching", 10, 2);
	public static final ZhaPin huanYuan = new ExHuanYuan("rejuvenation", 11, 2);
	public static final ZhaPin wenYa = new ExWenYa("thermobaric", 12, 2);

	public static final int E_ER_ID = 13;

	public static final ZhaPin yuanZi = new ExYuanZi("nuclear", 13, 3);
	public static final ZhaPin dianCi = new ExDianCi("emp", 14, 3);
	public static final ZhaPin taiYang = new ExTaiYang("exothermic", 15, 3);
	public static final ZhaPin bingDan = new ExBingDan("endothermic", 16, 3);
	public static final ZhaPin piaoFu = new ExPiaoFu("antiGravitational", 17, 3);
	public static final ZhaPin wanDan = new ExWan("ender", 18, 3);
	public static final ZhaPin chaoShengBuo = new ExChaoShengBuo("hypersonic", 19, 3);

	public static final int E_SAN_ID = 20;

	public static final ZhaPin fanWuSu = new ExFanWuSu("antimatter", 20, 4);
	public static final ZhaPin hongSu = new ExHongSu("redMatter", 21, 4);

	public static final int E_SI_ID = 22;

	public static final ZhaPin diLei = new ExDiLei("sMine", 25, 2);

	// Hidden Explosives
	public static final ZhaPin dianCiWave = new ExDianCiWave("emp", 26, 3);
	public static final ZhaPin dianCiSignal = new ExDianCiSignal("emp", 27, 3);
	public static final ZhaPin taiYang2 = new ExTaiYang2("conflagration", 28, 3);
	public static final ZhaPin fuLan = new ExFuLan("decayLand", 29, 3);
	public static final ZhaPin bianZhong = new ExBianZhong("mutateLiving", 30, 3);
	public static final ZhaPin bingDan2 = new ExBingDan2("endothermic", 31, 3);

	public static ZhaPin[] list;

	private String mingZi;
	private int ID;
	private int tier;
	private int yinXin;
	private DaoDan daoDan;
	protected boolean isDisabled;
	protected boolean isMobile = false;

	protected ZhaPin(String name, int ID, int tier)
	{
		if (list == null)
		{
			list = new ZhaPin[32];
		}

		if (list[ID] != null)
		{
			throw new IllegalArgumentException("Explosive " + ID + " is already occupied by " + list[ID].getClass().getSimpleName() + "!");
		}

		list[ID] = this;
		this.mingZi = name;
		this.tier = tier;
		this.yinXin = 100;
		this.ID = ID;
		this.daoDan = new DaoDan(name, ID, tier);

		ICBM.CONFIGURATION.load();
		this.isDisabled = ICBM.CONFIGURATION.get("Disable Explosive", "Disable " + this.mingZi, false).getBoolean(false);
		ICBM.CONFIGURATION.save();
	}

	@Override
	public int getID()
	{
		return this.ID;
	}

	@Override
	public String getName()
	{
		return this.mingZi;
	}

	@Override
	public String getExplosiveName()
	{
		return TranslationHelper.getLocal("icbm.explosive." + this.mingZi + ".name");
	}

	@Override
	public String getGrenadeName()
	{
		return TranslationHelper.getLocal("icbm.grenade." + this.mingZi + ".name");
	}

	@Override
	public String getMissileName()
	{
		return TranslationHelper.getLocal("icbm.missile." + this.mingZi + ".name");
	}

	@Override
	public String getMinecartName()
	{
		return TranslationHelper.getLocal("icbm.minecart." + this.mingZi + ".name");
	}

	@Override
	public int getTier()
	{
		return this.tier;
	}

	@Override
	public void setTier(int tier)
	{
		this.tier = tier;
	}

	public void setYinXin(int fuse)
	{
		this.yinXin = fuse;
	}

	/**
	 * The fuse of the explosion
	 * 
	 * @return The Fuse
	 */
	public int getYinXin()
	{
		return yinXin;
	}

	/**
	 * Called at the start of a detontation
	 * 
	 * @param worldObj
	 * @param entity
	 */
	public void yinZhaQian(World worldObj, Entity entity)
	{
		worldObj.playSoundAtEntity(entity, "random.fuse", 1.0F, 1.0F);
	}

	/**
	 * Called when the explosive is on fuse and going to explode. Called only when the explosive is
	 * in it's TNT form.
	 * 
	 * @param fuseTicks - The amount of ticks this explosive is on fuse
	 */
	public void onYinZha(World worldObj, Vector3 position, int fuseTicks)
	{
		worldObj.spawnParticle("smoke", position.x, position.y + 0.5D, position.z, 0.0D, 0.0D, 0.0D);
	}

	/**
	 * Called when the TNT for of this explosive is destroy by an explosion
	 * 
	 * @return - Fuse left
	 */
	public int onBeiZha()
	{
		return (int) (this.yinXin / 2 + Math.random() * this.yinXin / 4);
	}

	/**
	 * The interval in ticks before the next procedural call of this explosive
	 * 
	 * @param return - Return -1 if this explosive does not need procedural calls
	 */
	protected int proceduralInterval()
	{
		return -1;
	}

	public int proceduralInterval(World worldObj, int callCounts)
	{
		return this.proceduralInterval();
	}

	/**
	 * Called before an explosion happens
	 */
	public void baoZhaQian(World worldObj, Vector3 position, Entity explosionSource)
	{
		MinecraftForge.EVENT_BUS.post(new PreExplosionEvent(worldObj, position.x, position.y, position.z, this));
	}

	@SideOnly(Side.CLIENT)
	public Object[] getRenderData()
	{
		return null;
	}

	/**
	 * Called to do an explosion
	 * 
	 * @param explosionSource - The entity that did the explosion
	 * @param haoMa - The metadata of the explosive
	 * @param callCount - The amount of calls done for calling this explosion. Use only by
	 * procedural explosions
	 * @return - True if this explosive needs to continue to procedurally explode. False if
	 * otherwise
	 */
	public void doBaoZha(World worldObj, Vector3 position, Entity explosionSource)
	{
	}

	public boolean doBaoZha(World worldObj, Vector3 position, Entity explosionSource, int callCount)
	{
		doBaoZha(worldObj, position, explosionSource);
		return false;
	}

	public boolean doBaoZha(World worldObj, Vector3 position, Entity explosionSource, int metadata, int callCount)
	{
		return doBaoZha(worldObj, position, explosionSource, callCount);
	}

	/**
	 * Called every tick when this explosive is doing it's procedural explosion
	 * 
	 * @param ticksExisted - The ticks in which this explosive existed
	 */
	public void gengXin(World worldObj, Vector3 position, int ticksExisted)
	{
	}

	/**
	 * Called after the explosion is completed
	 */
	public void baoZhaHou(World worldObj, Vector3 position, Entity explosionSource)
	{
		MinecraftForge.EVENT_BUS.post(new PostExplosionEvent(worldObj, position.x, position.y, position.z, this));
	}

	public int countIncrement()
	{
		return 1;
	}

	/**
	 * Spawns an explosive (TNT form) in the world
	 * 
	 * @param worldObj
	 * @param position
	 * @param cause - 0: N/A, 1: Destruction, 2: Fire
	 */
	public void spawnZhaDan(World worldObj, Vector3 position, ForgeDirection orientation, byte cause)
	{
		if (!this.isDisabled)
		{
			position.add(0.5D);
			EZhaDan eZhaDan = new EZhaDan(worldObj, position, (byte) orientation.ordinal(), this.getID());

			switch (cause)
			{
				case 1:
					eZhaDan.destroyedByExplosion();
					break;
				case 2:
					eZhaDan.setFire(10);
					break;
			}

			worldObj.spawnEntityInWorld(eZhaDan);
		}
	}

	public void spawnZhaDan(World worldObj, Vector3 position, byte orientation)
	{
		this.spawnZhaDan(worldObj, position, ForgeDirection.getOrientation(orientation), (byte) 0);
	}

	/**
	 * Called to add the recipe for this explosive
	 */
	public void init()
	{
	};

	public ItemStack getItemStack()
	{
		return new ItemStack(ZhuYaoExplosion.bZhaDan, 1, this.getID());
	}

	public ItemStack getItemStack(int amount)
	{
		return new ItemStack(ZhuYaoExplosion.bZhaDan, amount, this.getID());
	}

	/**
	 * Created an ICBM explosion.
	 * 
	 * @param entity - The entity that created this explosion. The explosion source.
	 * @param explosiveID - The ID of the explosive.
	 */
	public static void createExplosion(World worldObj, double x, double y, double z, Entity entity, int explosiveID)
	{
		createBaoZha(worldObj, new Vector3(x, y, z), entity, explosiveID);
	}

	public static void createBaoZha(World worldObj, Vector3 position, Entity entity, int explosiveID)
	{
		if (!list[explosiveID].isDisabled)
		{
			if (list[explosiveID].proceduralInterval(worldObj, -1) > 0)
			{
				if (!worldObj.isRemote)
				{
					worldObj.spawnEntityInWorld(new EZhaPin(worldObj, position.clone(), explosiveID, list[explosiveID].isMobile));
				}
			}
			else
			{
				list[explosiveID].baoZhaQian(worldObj, position.clone(), entity);
				list[explosiveID].doBaoZha(worldObj, position.clone(), entity, explosiveID, -1);
				list[explosiveID].baoZhaHou(worldObj, position.clone(), entity);
			}
		}
	}

	public void doDamageEntities(World worldObj, Vector3 position, float radius, float power)
	{
		this.doDamageEntities(worldObj, position, radius, power, true);
	}

	public void doDamageEntities(World worldObj, Vector3 position, float radius, float power, boolean destroyItem)
	{
		// Step 2: Damage all entities
		radius *= 2.0F;
		Vector3 minCoord = position.clone();
		minCoord.add(-radius - 1);
		Vector3 maxCoord = position.clone();
		maxCoord.add(radius + 1);
		List allEntities = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(minCoord.intX(), minCoord.intY(), minCoord.intZ(), maxCoord.intX(), maxCoord.intY(), maxCoord.intZ()));
		Vec3 var31 = Vec3.createVectorHelper(position.x, position.y, position.z);

		for (int i = 0; i < allEntities.size(); ++i)
		{
			Entity entity = (Entity) allEntities.get(i);

			if (this.onDamageEntity(entity))
			{
				continue;
			}

			if (entity instanceof EDaoDan)
			{
				((EDaoDan) entity).explode();
				continue;
			}

			if (entity instanceof EntityItem && !destroyItem)
				continue;

			double distance = entity.getDistance(position.x, position.y, position.z) / radius;

			if (distance <= 1.0D)
			{
				double xDifference = entity.posX - position.x;
				double yDifference = entity.posY - position.y;
				double zDifference = entity.posZ - position.z;
				double var35 = MathHelper.sqrt_double(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
				xDifference /= var35;
				yDifference /= var35;
				zDifference /= var35;
				double var34 = worldObj.getBlockDensity(var31, entity.boundingBox);
				double var36 = (1.0D - distance) * var34;
				int damage = 0;

				damage = (int) ((var36 * var36 + var36) / 2.0D * 8.0D * power + 1.0D);

				entity.attackEntityFrom(DamageSource.explosion, damage);

				entity.motionX += xDifference * var36;
				entity.motionY += yDifference * var36;
				entity.motionZ += zDifference * var36;
			}
		}
	}

	/**
	 * Called by doDamageEntity on each entity being damaged. This function should be inherited if
	 * something special is to happen to a specific entity.
	 * 
	 * @return True if something special happens to this specific entity.
	 */
	protected boolean onDamageEntity(Entity entity)
	{
		return false;
	}
}
