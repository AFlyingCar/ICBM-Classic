package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.PotionInfoHelper;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;

import java.util.List;

public class ItemPotionMissile extends ItemMissile {
    public ItemPotionMissile() {
        super("potionmissile");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTab())
        {
            if (Explosives.POTION.handler.hasMissileForm())
            {
                items.add(new ItemStack(this, 1, Explosives.POTION.ordinal()));
            }
        }
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        super.getDetailedInfo(stack, player, list);

        if(stack.getItemDamage() == Explosives.POTION.ordinal()) {
            List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
            PotionEffect effect = effects.isEmpty() ? null : effects.get(0);

            PotionInfoHelper.addPotionInfo(effect, list);
        }
    }
}
