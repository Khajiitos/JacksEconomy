package me.khajiitos.jackseconomy.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class NewShopUnlocks {
    public Set<String> unlockedCategories = new HashSet<>();
    public Set<Item> unlockedItems = new HashSet<>();

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        ListTag items = new ListTag();
        ListTag categories = new ListTag();

        unlockedItems.forEach(item -> {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("slot", item.slot);
            itemTag.putString("category", item.category);
            items.add(itemTag);
        });

        unlockedCategories.forEach(category -> categories.add(StringTag.valueOf(category)));

        tag.put("items", items);
        tag.put("categories", categories);

        return tag;
    }

    public static NewShopUnlocks fromNbt(CompoundTag nbt) {
        NewShopUnlocks newShopUnlocks = new NewShopUnlocks();

        if (nbt.contains("items", Tag.TAG_LIST) && nbt.contains("categories", Tag.TAG_LIST)) {
            ListTag items = nbt.getList("items", Tag.TAG_COMPOUND);
            ListTag categories = nbt.getList("categories", Tag.TAG_STRING);

            items.forEach(tag -> {
                if (tag instanceof CompoundTag compoundTag) {
                    int slot = compoundTag.getInt("slot");
                    String category = compoundTag.getString("category");

                    newShopUnlocks.unlockedItems.add(new Item(slot, category));
                }
            });

            categories.forEach(tag -> {
                if (tag instanceof StringTag stringTag) {
                    newShopUnlocks.unlockedCategories.add(stringTag.getAsString());
                }
            });
        }

        return newShopUnlocks;
    }

    public void merge(NewShopUnlocks another) {
        // It's a set, duplicates shouldn't happen, right?
        unlockedItems.addAll(another.unlockedItems);
        unlockedCategories.addAll(another.unlockedCategories);
    }

    public void reduce(NewShopUnlocks acknowledgedUnlocks) {
        unlockedItems.removeIf(acknowledgedUnlocks.unlockedItems::contains);
        unlockedCategories.removeIf(acknowledgedUnlocks.unlockedCategories::contains);
    }

    public boolean isEmpty() {
        return unlockedCategories.isEmpty() && unlockedItems.isEmpty();
    }

    public record Item(int slot, String category) { }
}