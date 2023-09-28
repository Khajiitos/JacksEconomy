package me.khajiitos.jackseconomy.item;

import me.khajiitos.jackseconomy.price.ItemDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class TicketItem extends Item {
    public TicketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public TicketItem(Item.Properties properties) {
        super(properties);
    }

    public static List<ItemDescription> getItems(ItemStack itemStack) {
        List<ItemDescription> list = new ArrayList<>();

        if (!(itemStack.getItem() instanceof TicketItem)) {
            return list;
        }

        CompoundTag nbtTag = itemStack.getTag();

        if (nbtTag == null) {
            return list;
        }

        ListTag listTag = nbtTag.getList("Items", Tag.TAG_COMPOUND);
        listTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);
                if (itemDescription != null) {
                    list.add(itemDescription);
                }
            }
        });
        return list;
    }

    public static void setItems(ItemStack itemStack, List<ItemDescription> items) {
        ListTag tag = new ListTag();
        items.forEach(s -> tag.add(s.toNbt()));
        itemStack.getOrCreateTag().put("Items", tag);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        List<ItemDescription> itemDescriptions = getItems(pStack);

        for (ItemDescription itemDescription : itemDescriptions) {
            if (itemDescription.item() != Items.AIR) {
                pTooltipComponents.add(Component.literal("- ").append(itemDescription.item().getDescription().copy()).withStyle(ChatFormatting.AQUA));
            }
        }
    }
}
