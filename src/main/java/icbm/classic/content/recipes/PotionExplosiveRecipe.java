package icbm.classic.content.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.BlockPotionExplosive;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// public class PotionExplosiveRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
public class PotionExplosiveRecipe extends ShapelessOreRecipe {
    public PotionExplosiveRecipe(ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result) {
        super(group, input, result);
    }

    protected boolean isPotion(ItemStack stack) {
        return stack.getItem().equals(Items.POTIONITEM) || stack.getItem().equals(Items.LINGERING_POTION) /*||
               stack.getItem().equals(Items.SPLASH_POTION)*/;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean foundCondensedExplosive = false;
        boolean foundPotion = false;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty()) {
                if(stack.getItem() instanceof ItemBlockExplosive) {
                    Explosives ex = Explosives.get(stack.getItemDamage());

                    if(ex == Explosives.CONDENSED) {
                        // Doesn't match if we've found condensed already
                        if(foundCondensedExplosive)
                            return false;
                        foundCondensedExplosive = true;
                    }
                } else if(stack.getItem() instanceof ItemPotion) {
                    // Doesn't match if we've found a potion already
                    if(foundPotion) return false;

                    foundPotion = true;
                } else {
                    return false;
                }
            }
        }

        return foundCondensedExplosive && foundPotion;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack potionStack = ItemStack.EMPTY;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty()) {
                if(isPotion(stack))
                    potionStack = stack.copy();
            }
        }

        // Sanity check
        if(potionStack.isEmpty()) {
            return potionStack;
        }

        // TODO: Get potion fluid???

        ItemStack potionExplosiveStack = new ItemStack(Item.getItemFromBlock(ICBMClassic.blockPotionExplosive), 1);
        potionExplosiveStack.setItemDamage(Explosives.POTION.ordinal());
        PotionUtils.addPotionToItemStack(potionExplosiveStack, PotionUtils.getPotionFromItem(potionStack));
        PotionUtils.appendEffects(potionExplosiveStack, PotionUtils.getFullEffectsFromItem(potionStack));

        return potionExplosiveStack;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for(int i = 0; i < remaining.size(); ++i) {
            if(isPotion(inv.getStackInSlot(i)))
                remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
        }

        return remaining;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public String getGroup() {
        return group == null ? "" : group.toString();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    public static class Factory implements IRecipeFactory {
        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            final String group = JsonUtils.getString(json, "group", "");
            final NonNullList<Ingredient> ingredients = parseShapeless(context, json);
            final ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);

            return new PotionExplosiveRecipe(group.isEmpty() ? null : new ResourceLocation(group), ingredients, result);
        }

        protected static NonNullList<Ingredient> parseShapeless(final JsonContext context, final JsonObject json) {
            final NonNullList<Ingredient> ingredients = NonNullList.create();

            for(final JsonElement element : JsonUtils.getJsonArray(json, "ingredients")) {
                ingredients.add(CraftingHelper.getIngredient(element, context));
            }

            if(ingredients.isEmpty())
                throw new JsonParseException("No ingredients for shapeless recipe.");
            return ingredients;
        }
    }
}
