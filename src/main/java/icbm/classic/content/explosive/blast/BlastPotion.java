package icbm.classic.content.explosive.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.client.ICBMSounds;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class BlastPotion extends Blast {
    public static final int PARTICLES_TO_SPAWN = 200; // TODO: Add to a config
    public static final int TICKS_BETWWEEN_RUNS = 5;

    private int duration;
    /** Color of particles */
    private float red = 1;
    private float green = 1;
    private float blue = 1;

    private boolean playShortSoundFX;
    private boolean isMutate;
    private PotionEffect potionEffect;

    private Set<EntityLivingBase> appliedEntities;

    public BlastPotion(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastPotion(World world, Entity entity, double x, double y, double z, float size, int duration, boolean playShortSoundFX) {
        this(world, entity, x, y, z, size);
        this.duration = duration / this.proceduralInterval();
        this.playShortSoundFX = playShortSoundFX;
    }

    public BlastPotion setRGB(float r, float g, float b) {
        red = r;
        green = g;
        blue = b;

        return this;
    }

    public BlastPotion setPotionEffect(PotionEffect effect) {
        potionEffect = effect;
        return this;
    }

    @Override
    public void doPreExplode() {
        super.doPreExplode();
        if(!this.playShortSoundFX)
            ICBMSounds.DEBILITATION.play(world, location.x(), location.y(), location.z(), 4.0F, (1.0F + (world().rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F, true);
    }

    @Override
    public void doExplode() {
        final float radius = getBlastRadius();

        generateGraphicEffect();
        generateAudioEffect();

        // Bounding box for those affected by the potion
        AxisAlignedBB bounds = new AxisAlignedBB(location.x() - radius, location.y() - radius, location.z() - radius,
                                                 location.x() + radius, location.y() + radius, location.z() + radius);

        List<EntityLivingBase> allEntities = world().getEntitiesWithinAABB(EntityLivingBase.class, bounds);

        if(potionEffect != null) {
            for(EntityLivingBase entity : allEntities) {
                // Same code as what happens when hit with a splash potion
                if(entity.canBeHitWithPotion()) {
                    double d1 = 1.0D - Math.sqrt(entity.getDistanceSq(this.getPos())) / 4.0D;
                    if(potionEffect.getPotion().isInstant()) {
                        potionEffect.getPotion().affectEntity(this.controller, null, entity, potionEffect.getAmplifier(), d1);
                    } else {
                        // int i = (int) (d1 * (double) potionEffect.getDuration() + 0.5D);
                        // if(i > 20)
                        entity.addPotionEffect(new PotionEffect(potionEffect.getPotion(), potionEffect.getDuration(), potionEffect.getAmplifier(), potionEffect.getIsAmbient(), potionEffect.doesShowParticles()));
                    }
                }
            }
        }

        if(isMutate)
            new BlastMutation(world(), this.exploder, location.x(), location.y(), location.z(), radius).runBlast();

        if(callCount > duration)
            controller.endExplosion();
    }

    @Override
    public void doPostExplode() {
    }

    protected void generateAudioEffect() {
        if(playShortSoundFX)
            ICBMSounds.GAS_LEAK.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D, 4.0F,
                               (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
    }

    protected void generateGraphicEffect() {
        if(world().isRemote) {
            final float radius = getBlastRadius();
            for(int i = 0; i < PARTICLES_TO_SPAWN; ++i) {
                Pos randomSpawnPoint = new Pos(Math.random() * radius / 2 - radius / 4, Math.random() * radius / 2 - radius / 4, Math.random() * radius / 2 - radius / 4);

                randomSpawnPoint = randomSpawnPoint.multiply(Math.min(radius, callCount) / 10);

                if(randomSpawnPoint.magnitude() <= radius) {
                    randomSpawnPoint = randomSpawnPoint.add(location);

                    ICBMClassic.proxy.spawnSmoke(world(), randomSpawnPoint, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, red, green, blue, 7.0F, 100);
                }
            }
        }
    }

    @Override
    public int proceduralInterval() {
        return TICKS_BETWWEEN_RUNS;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        duration = nbt.getInteger("duration");
        isMutate = nbt.getBoolean("isMutate");
        red = nbt.getFloat("red");
        green = nbt.getFloat("green");
        blue = nbt.getFloat("blue");
        playShortSoundFX = nbt.getBoolean("playShortSoundFX");

        if(nbt.hasKey("effect")) {
            potionEffect = PotionEffect.readCustomPotionEffectFromNBT(nbt.getCompoundTag("effect"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("duration", duration);
        nbt.setBoolean("isMutate", isMutate);
        nbt.setFloat("red", red);
        nbt.setFloat("green", green);
        nbt.setFloat("blue", blue);
        nbt.setBoolean("playShortSoundFX", playShortSoundFX);

        if(potionEffect != null) {
            NBTTagCompound effectTag = new NBTTagCompound();
            potionEffect.writeCustomPotionEffectToNBT(effectTag);
            nbt.setTag("effect", effectTag);
        }
    }

    @Override
    public String toString() {
        return (potionEffect != null ? I18n.translateToLocal(potionEffect.getEffectName()) : I18n.translateToLocal("effect.none")).trim();
    }
}
