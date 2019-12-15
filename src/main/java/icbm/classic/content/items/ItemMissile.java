package icbm.classic.content.items;

import icbm.classic.lib.LanguageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import icbm.classic.prefab.tile.EnumTier;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

public class ItemMissile extends ItemICBMBase
{
    public ItemMissile()
    {
        this("missile");
    }

    public ItemMissile(String name) {

        super(name);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getTranslationKey(ItemStack itemStack)
    {
        return this.getTranslationKey() + "." + Explosives.get(itemStack.getItemDamage()).handler.getTranslationKey();
    }

    @Override
    public String getTranslationKey()
    {
        return "icbm.missile";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTab())
        {
            for (Explosives explosive : Explosives.values())
            {
                if (explosive.handler.hasMissileForm())
                {
                    items.add(new ItemStack(this, 1, explosive.ordinal()));
                }
            }
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        EnumTier tierdata = Explosives.get(stack.getItemDamage()).handler.getTier();
        list.add(LanguageUtility.getLocal("info.misc.tier") + ": " + tierdata.getName());
        if (ICBMClassic.blockExplosive instanceof BlockExplosive)
        {
            ((ItemBlockExplosive) Item.getItemFromBlock(ICBMClassic.blockExplosive)).getDetailedInfo(stack, player, list);
        }
    }
}
