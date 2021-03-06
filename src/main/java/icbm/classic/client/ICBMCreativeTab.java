package icbm.classic.client;

import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.List;

/**
 * Prefab creative tab to either create a fast creative tab or reduce code
 * need to make a more complex tab
 * Created by robert on 11/25/2014.
 */
public class ICBMCreativeTab extends CreativeTabs
{
    public ItemStack itemStack = null;
    public List<Item> definedTabItemsInOrder = new ArrayList();

    public ICBMCreativeTab(String name)
    {
        super(name);
    }

    public void init()
    {
        definedTabItemsInOrder.clear();
        orderItem(ICBMClassic.blockLaunchBase);
        orderItem(ICBMClassic.blockLaunchScreen);
        orderItem(ICBMClassic.blockLaunchSupport);
        orderItem(ICBMClassic.blockEmpTower);
        orderItem(ICBMClassic.blockRadarStation);
        orderItem(ICBMClassic.blockBattery);

        orderItem(ICBMClassic.blockConcrete);
        orderItem(ICBMClassic.blockReinforcedGlass);
        orderItem(ICBMClassic.blockSpikes);

        orderItem(ICBMClassic.itemRocketLauncher);
        orderItem(ICBMClassic.itemRadarGun);
        orderItem(ICBMClassic.itemRemoteDetonator);
        orderItem(ICBMClassic.itemLaserDesignator);
        orderItem(ICBMClassic.itemTracker);
        orderItem(ICBMClassic.itemSignalDisrupter);
        orderItem(ICBMClassic.itemDefuser);
        orderItem(ICBMClassic.itemBattery);

        orderItem(ICBMClassic.blockExplosive);
        orderItem(ICBMClassic.itemMissile);
        orderItem(ICBMClassic.itemGrenade);
        orderItem(ICBMClassic.itemBombCart);
    }

    private void orderItem(Block item)
    {
        orderItem(Item.getItemFromBlock(item));
    }

    private void orderItem(Item item)
    {
        definedTabItemsInOrder.add(item);
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> list)
    {
        //Insert defined items in order
        definedTabItemsInOrder.forEach(item -> collectSubItems(item, list));

        //Collect any non-defined items
        for (Item item : Item.REGISTRY)
        {
            if (item != null)
            {
                for (CreativeTabs tab : item.getCreativeTabs())
                {
                    if (tab == this && !definedTabItemsInOrder.contains(item))
                    {
                        collectSubItems(item, list);
                    }
                }
            }
        }
    }

    protected void collectSubItems(Item item, NonNullList<ItemStack> list)
    {
        //Collect stacks
        NonNullList<ItemStack> temp_list = NonNullList.create();
        item.getSubItems(this, temp_list);

        //Merge into list with null check
        mergeIntoList(item, list, temp_list);
    }

    protected void mergeIntoList(Item item, NonNullList<ItemStack> list, NonNullList<ItemStack> listToMerge)
    {
        for (ItemStack stack : listToMerge)
        {
            if (stack.getItem() != null)
            {
                list.add(stack);
            }
            else
            {
                ICBMClassic.logger().error("Item: " + item + "  attempted to add a stack with a null item to creative tab " + this);
            }
        }
    }

    @Override
    public ItemStack createIcon()
    {
        if (itemStack == null || itemStack.getItem() == null)
        {
            //Display error for devs to see
            if (itemStack == null)
            {
                ICBMClassic.logger().error("ItemStack used for creative tab " + this.getTabLabel() + " is null");
            }
            else
            {
                ICBMClassic.logger().error("ItemStack used for creative tab " + this.getTabLabel() + " contains a null Item reference");
            }

            //Attempt to use a random item in the tab
            NonNullList<ItemStack> list = NonNullList.create();
            displayAllRelevantItems(list);
            for (ItemStack stack : list)
            {
                itemStack = stack;
                return itemStack;
            }

            //If that fails set to redstone block as backup
            itemStack = new ItemStack(Blocks.REDSTONE_LAMP);
        }
        return itemStack;
    }

    /**
     * Helper method to add the item and it's sub types to a list
     *
     * @param list
     * @param item
     */
    protected void add(NonNullList<ItemStack> list, Item item)
    {
        if (item != null)
        {
            item.getSubItems(this, list);
        }
    }

    /**
     * Helper method to add the item and it's sub types to a list
     *
     * @param list
     * @param block
     */
    protected void add(NonNullList<ItemStack> list, Block block)
    {
        if (block != null)
        {
            block.getSubBlocks(this, list);
        }
    }
}
