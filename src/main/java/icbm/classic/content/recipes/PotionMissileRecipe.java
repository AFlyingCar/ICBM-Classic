package icbm.classic.content.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.ItemBlockPotionExplosive;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

public class PotionMissileRecipe extends ShapelessOreRecipe {
    public PotionMissileRecipe(ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result) {
        super(group, input, result);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean foundMissileModule = false;
        boolean foundPotionExplosive = false;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty()) {
                if(stack.getItem() == ICBMClassic.itemMissile && stack.getItemDamage() == Explosives.MISSILE.ordinal()) {
                    if(foundMissileModule) return false;

                    foundMissileModule = true;
                } else if(isPotionBlock(stack)) {
                    if(foundPotionExplosive) return false;
                    foundPotionExplosive = true;
                } else {
                    return false;
                }
            }
        }

        return foundMissileModule && foundPotionExplosive;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack potionBlockStack = ItemStack.EMPTY;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if(!stack.isEmpty()) {
                if(isPotionBlock(stack))
                    potionBlockStack = stack.copy();
            }
        }

        if(potionBlockStack.isEmpty()) return ItemStack.EMPTY;

        ItemStack potionMissileStack = new ItemStack(ICBMClassic.itemPotionMissile, 1);
        potionMissileStack.setItemDamage(Explosives.POTION.ordinal());
        PotionUtils.addPotionToItemStack(potionMissileStack, PotionUtils.getPotionFromItem(potionBlockStack));
        PotionUtils.appendEffects(potionMissileStack, PotionUtils.getFullEffectsFromItem(potionBlockStack));

        return potionMissileStack;
    }

    private boolean isPotionBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlockPotionExplosive && stack.getItemDamage() == Explosives.POTION.ordinal();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ICBMClassic.itemMissile, 1);
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

            return new PotionMissileRecipe(group.isEmpty() ? null : new ResourceLocation(group), ingredients, result);
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
