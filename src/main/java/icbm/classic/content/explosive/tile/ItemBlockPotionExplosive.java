package icbm.classic.content.explosive.tile;

import com.google.common.collect.Lists;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.lib.PotionInfoHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemBlockPotionExplosive extends ItemBlockExplosive {
    public ItemBlockPotionExplosive(Block block) {
        super(block);
    }

    @Override
    public void getDetailedInfo(ItemStack stack, @Nullable EntityPlayer player, List list) {
        super.getDetailedInfo(stack, player, list);

        if(stack.getItemDamage() == Explosives.POTION.ordinal()) {
            List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
            PotionEffect effect = effects.isEmpty() ? null : effects.get(0);

            PotionInfoHelper.addPotionInfo(effect, list);
        }
    }

    @Override
    public String getTranslationKey(ItemStack itemstack)
    {
        return this.getTranslationKey() + "." + Explosives.get(itemstack.getItemDamage()).handler.getTranslationKey();
    }

    @Override
    public String getTranslationKey()
    {
        return "icbm.explosive";
    }
}
