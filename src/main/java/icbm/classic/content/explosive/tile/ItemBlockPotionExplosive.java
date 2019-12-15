package icbm.classic.content.explosive.tile;

import com.google.common.collect.Lists;
import icbm.classic.content.explosive.Explosives;
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

            List<Tuple<String, AttributeModifier>> modifiers = Lists.<Tuple<String, AttributeModifier>>newArrayList();
            if(effect == null) {
                String s = I18n.translateToLocal("effect.none").trim();
                list.add(TextFormatting.GRAY + s);
            } else {
                Map<IAttribute, AttributeModifier> attribMap = effect.getPotion().getAttributeModifierMap();
                for(Map.Entry<IAttribute, AttributeModifier> entry : attribMap.entrySet()) {
                    AttributeModifier attributeModifier = entry.getValue();
                    AttributeModifier attributeModifier1 = new AttributeModifier(attributeModifier.getName(), effect.getPotion().getAttributeModifierAmount(effect.getAmplifier(), attributeModifier), attributeModifier.getOperation());
                    modifiers.add(new Tuple<>(((IAttribute)entry.getKey()).getName(), attributeModifier1));
                }

                String s = I18n.translateToLocal(effect.getEffectName()).trim();

                if(effect.getAmplifier() > 0)
                    s = s + " " + I18n.translateToLocal("potion.potency." + effect.getAmplifier()).trim();
                if(effect.getDuration() > 20)
                    s = s + " (" + Potion.getPotionDurationString(effect, 1.0F) + ")";
                if(effect.getPotion().isBadEffect())
                    list.add(TextFormatting.RED + s);
                else
                    list.add(TextFormatting.BLUE + s);
            }

            if(!modifiers.isEmpty()) {
                list.add("");
                list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("potion.whenDrank"));

                for(Tuple<String, AttributeModifier> tuple : modifiers) {
                    AttributeModifier attrMod2 = tuple.getSecond();
                    double d0 = attrMod2.getAmount();
                    double d1;

                    if (attrMod2.getOperation() != 1 && attrMod2.getOperation() != 2)
                    {
                        d1 = attrMod2.getAmount();
                    }
                    else
                    {
                        d1 = attrMod2.getAmount() * 100.0D;
                    }

                    if (d0 > 0.0D)
                    {
                        list.add(TextFormatting.BLUE + I18n.translateToLocalFormatted("attribute.modifier.plus." + attrMod2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + (String)tuple.getFirst())));
                    }
                    else if (d0 < 0.0D)
                    {
                        d1 = d1 * -1.0D;
                        list.add(TextFormatting.RED + I18n.translateToLocalFormatted("attribute.modifier.take." + attrMod2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + (String)tuple.getFirst())));
                    }
                }
            }
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
