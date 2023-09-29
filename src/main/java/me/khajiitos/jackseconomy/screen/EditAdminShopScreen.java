package me.khajiitos.jackseconomy.screen;

import com.google.gson.JsonArray;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.UpdateAdminShopPacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.EditCategoryEntry;
import me.khajiitos.jackseconomy.screen.widget.FloatingEditBoxWidget;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;

public class EditAdminShopScreen extends AdminShopScreen {
    protected ShopItem itemOnCursor;
    protected FloatingEditBoxWidget floatingEditBox;

    public EditAdminShopScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

        pMenu.setSlotsLocked(true);
    }

    @Override
    protected boolean isHoverObstructed(int mouseX, int mouseY) {
        return this.floatingEditBox != null && mouseX >= this.floatingEditBox.x && mouseX <= this.floatingEditBox.x + this.floatingEditBox.getWidth() && mouseY >= this.floatingEditBox.y && mouseY <= this.floatingEditBox.y + this.floatingEditBox.getWidth();
    }

    @Override
    protected void initCategoryPanel() {

        if (this.categoryPanel != null) {
            this.removeWidget(this.categoryPanel);
        }

        this.categoryPanel = this.addRenderableWidget(new BetterScrollPanel(Minecraft.getInstance(), this.leftPos - 80, this.topPos + 20, 75, this.imageHeight - 40));

        for (Category category : shopItems.keySet()) {
            this.categoryPanel.children.add(new EditCategoryEntry(0, 0, 75, 25, category, (categoryEntry, button) -> {
                if (button == 0) {
                    if (this.itemOnCursor != null) {
                        category.item = this.itemOnCursor.itemDescription().item();
                        this.itemOnCursor = null;
                    } else {
                        this.selectBigCategory(category);
                    }
                } else if (button == 1) {
                    if (this.floatingEditBox != null) {
                        this.removeWidget(this.floatingEditBox);
                        this.floatingEditBox = null;
                    }

                    this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, categoryEntry.x + 37, categoryEntry.y + 20, 75, 15, (value) -> {
                        for (Category otherCategory : this.shopItems.keySet()) {
                            if (this.shopItems.size() > 1 && otherCategory == category) {
                                continue;
                            }

                            // Category names have to be unique
                            if (otherCategory.name.equals(value)) {
                                return;
                            }

                            category.name = value;

                            if (this.floatingEditBox != null) {
                                this.removeWidget(this.floatingEditBox);
                                this.floatingEditBox = null;
                            }

                            return;
                        }
                    }));
                    this.setFocused(this.floatingEditBox);
                } else if (button == 2) {
                    this.shopItems.remove(category);

                    if (this.selectedCategory == category) {
                        this.selectedCategory = null;
                        for (Category category1 : this.shopItems.keySet()) {
                            this.selectBigCategory(category1);
                            break;
                        }

                        if (this.selectedCategory == null) {
                            this.selectBigCategory(null);
                        }
                    }

                    this.initCategoryPanel();
                }
            }, () -> this.selectedBigCategory == category, () -> tooltip = List.of(Component.translatable("jackseconomy.right_click_to_rename").withStyle(ChatFormatting.AQUA), Component.translatable("jackseconomy.middle_click_to_remove_category").withStyle(ChatFormatting.RED))));
        }

        this.categoryPanel.children.add(new EditCategoryEntry(0, 0, 75, 25, null, (categoryEntry, button) -> {
            if (button == 0 && this.itemOnCursor != null) {
                Category category = new Category(getUnnamedCategoryName(), this.itemOnCursor.itemDescription().item());
                this.shopItems.put(category, new LinkedHashMap<>());
                this.initCategoryPanel();
                this.itemOnCursor = null;
            }
        }, () -> false, () -> tooltip = List.of(Component.translatable("jackseconomy.drop_item_to_create_category"))));
    }

    protected String getUnnamedInnerCategoryName() {
        List<InnerCategory> categories = this.shopItems.get(selectedBigCategory).keySet().stream().toList();
        int i = -1;
        while (true) {
            String name = "Unnamed" + (i == -1 ? "" : " " + i);
            boolean exists = false;

            for (InnerCategory category : categories) {
                if (category.name.equals(name)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                return name;
            }

            i++;
        }
    }

    protected String getUnnamedCategoryName() {
        List<Category> categories = this.shopItems.keySet().stream().toList();
        int i = -1;
        while (true) {
            String name = "Unnamed" + (i == -1 ? "" : " " + i);
            boolean exists = false;

            for (InnerCategory category : categories) {
                if (category.name.equals(name)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                return name;
            }

            i++;
        }
    }

    @Override
    protected boolean onCategorySlotClicked(int pButton, int categoryId) {
        if (pButton == 0) {
            List<InnerCategory> categories = this.shopItems.get(selectedBigCategory).keySet().stream().toList();

            if (categoryId == categories.size() && this.itemOnCursor != null && this.floatingEditBox == null) {
                InnerCategory category = new InnerCategory(this.getUnnamedInnerCategoryName(), this.itemOnCursor.itemDescription().item());

                if (this.selectedCategory == null) {
                    this.selectedCategory = category;
                }

                this.shopItems.get(selectedBigCategory).put(category, new ArrayList<>());

                int categoryRenderId = categoryId - this.categoryOffset;
                int categoryX = this.leftPos + 17 + categoryRenderId * 18, categoryY = this.topPos + 27;

                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, categoryX + 8, categoryY + 18, 50, 15, (value) -> {
                    for (InnerCategory otherCategory : this.shopItems.keySet()) {
                        if (otherCategory == category) {
                            continue;
                        }

                        // Category names have to be unique
                        if (otherCategory.name.equals(value)) {
                            return;
                        }

                        category.name = value;

                        if (this.floatingEditBox != null) {
                            this.removeWidget(this.floatingEditBox);
                            this.floatingEditBox = null;
                        }

                        return;
                    }
                }));
                this.setFocused(this.floatingEditBox);

                this.itemOnCursor = null;
                return true;
            }
        } else if (pButton == 1) {
            int categoryRenderId = categoryId - this.categoryOffset;
            int categoryX = this.leftPos + 17 + categoryRenderId * 18, categoryY = this.topPos + 27;

            InnerCategory category = this.getCategoryById(categoryId);
            if (category != null && this.floatingEditBox == null) {
                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, categoryX + 8, categoryY + 18, 50, 15, (value) -> {
                    category.name = value;
                    this.removeWidget(this.floatingEditBox);
                    this.floatingEditBox = null;
                }));
                this.setFocused(this.floatingEditBox);
            }
        } else if (pButton == 2) {
            InnerCategory category = this.getCategoryById(categoryId);

            if (category != null) {
                this.shopItems.get(selectedBigCategory).remove(category);

                if (this.selectedCategory == category) {
                    this.selectedCategory = null;
                }
            }
        }
        return super.onCategorySlotClicked(pButton, categoryId);
    }

    @Override
    protected void onSlotClicked(int slot, int pButton) {
        if (pButton == 0) {
            ShopItem onCursorBefore = this.itemOnCursor;
            if (onCursorBefore != null && onCursorBefore.price() == -1 && this.floatingEditBox == null) {
                Pair<Integer, Integer> slotPos = this.getSlotPos(slot);

                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, slotPos.getFirst() + 8, slotPos.getSecond() + 18, 50, 15, (value) -> {
                    try {
                        double newPrice = Double.parseDouble(value);

                        this.setItemAtSlot(new ShopItem(onCursorBefore.itemDescription(), newPrice, slot, onCursorBefore.customName()), slot, this.selectedCategory);
                        this.removeWidget(this.floatingEditBox);
                        this.floatingEditBox = null;
                    } catch (NumberFormatException ignored) {}
                }));
                this.setFocused(this.floatingEditBox);
            }
            this.itemOnCursor = this.setItemAtSlot(this.itemOnCursor, slot, this.selectedCategory);
        } else if (pButton == 1) {
            if (this.floatingEditBox != null) {
                this.removeWidget(this.floatingEditBox);
                this.floatingEditBox = null;
                return;
            }

            ShopItem itemAtSlot = this.getItemAtSlot(slot, this.selectedCategory);

            if (itemAtSlot == null) {
                return;
            }

            Pair<Integer, Integer> slotPos = this.getSlotPos(slot);

            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, slotPos.getFirst() + 8, slotPos.getSecond() + 16, 50, 15, (value) -> {
                    if (!value.isEmpty()) {
                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), itemAtSlot.price(), slot, value), slot, this.selectedCategory);
                    } else {
                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), itemAtSlot.price(), slot, null), slot, this.selectedCategory);
                    }
                    this.removeWidget(this.floatingEditBox);
                    this.floatingEditBox = null;
                }));

                if (itemAtSlot.customName() != null) {
                    this.floatingEditBox.setValue(itemAtSlot.customName());
                }
            } else {
                this.floatingEditBox = this.addRenderableWidget(new FloatingEditBoxWidget(this.font, slotPos.getFirst() + 8, slotPos.getSecond() + 16, 50, 15, (value) -> {
                    try {
                        double newPrice = Double.parseDouble(value);

                        this.setItemAtSlot(new ShopItem(itemAtSlot.itemDescription(), newPrice, slot, itemAtSlot.customName()), slot, this.selectedCategory);
                        this.removeWidget(this.floatingEditBox);
                        this.floatingEditBox = null;
                    } catch (NumberFormatException ignored) {}
                }));
                this.floatingEditBox.setValue(String.format(Locale.US, "%.2f", itemAtSlot.price()));
            }
            this.setFocused(this.floatingEditBox);
        } else if (pButton == 2) {
            ShopItem itemAtSlot = this.getItemAtSlot(slot, this.selectedCategory);

            if (itemAtSlot == null) {
                return;
            }

            this.setItemAtSlot(null, slot, this.selectedCategory);
        }
    }

    @Override
    protected boolean isEditMode() {
        return true;
    }

    public CompoundTag toAdminShopUpdateCompound() {
        CompoundTag tag = new CompoundTag();

        ListTag itemsTag = new ListTag();
        ListTag categoriesTag = new ListTag();

        this.shopItems.forEach((category, innerCategories) -> {
            String itemName = ItemHelper.getItemName(category.item);

            if (itemName == null) {
                return;
            }

            CompoundTag categoryTag = new CompoundTag();
            categoryTag.putString("name", category.name);
            categoryTag.putString("item", itemName);

            ListTag innerCategoriesTag = new ListTag();

            for (Map.Entry<InnerCategory, List<ShopItem>> entry : innerCategories.entrySet()) {
                String innerItemName = ItemHelper.getItemName(entry.getKey().item);

                if (innerItemName == null) {
                    return;
                }

                CompoundTag innerCategoryTag = new CompoundTag();
                innerCategoryTag.putString("name", entry.getKey().name);
                innerCategoryTag.putString("item", innerItemName);

                for (ShopItem shopItem : entry.getValue()) {
                    CompoundTag itemTag = shopItem.itemDescription().toNbt();
                    itemTag.putDouble("adminShopBuyPrice", shopItem.price());
                    itemTag.putString("category", category.name + ":" + entry.getKey().name);
                    itemTag.putInt("slot", shopItem.slot());

                    if (shopItem.customName() != null) {
                        itemTag.putString("customAdminShopName", shopItem.customName());
                    }

                    itemsTag.add(itemTag);
                }

                innerCategoriesTag.add(innerCategoryTag);
            }

            categoryTag.put("categories", innerCategoriesTag);
            categoriesTag.add(categoryTag);
        });

        tag.put("items", itemsTag);
        tag.put("categories", categoriesTag);

        return tag;
    }

    private void sendChanges() {
        Packets.sendToServer(new UpdateAdminShopPacket(this.toAdminShopUpdateCompound()));
    }

    @Override
    public void renderStageTwo(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderStageTwo(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.itemOnCursor != null) {
            Minecraft.getInstance().getItemRenderer().renderGuiItem(this.itemOnCursor.itemDescription().createItemStack(), pMouseX - 8, pMouseY - 8);
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        if (this.itemOnCursor == null && pSlot != null) {
            ItemStack itemStack = pSlot.getItem();
            if (!itemStack.isEmpty()) {
                this.itemOnCursor = new ShopItem(ItemDescription.ofItem(itemStack), -1, -1, null);
                return;
            }
        } else if (pSlot == null) {
            this.itemOnCursor = null;

            if (this.floatingEditBox != null) {
                this.removeWidget(this.floatingEditBox);
                this.floatingEditBox = null;
            }
        }
        // shut up, pSlot CAN BE NULL!!!
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_LEFT) {
            if (this.getFocused() instanceof Widget widget && this.renderables.contains(widget)) {
                return false;
            }

            int categoryIndex = this.shopItems.get(selectedBigCategory).keySet().stream().toList().indexOf(this.selectedCategory);

            if (categoryIndex == -1 || categoryIndex == 0) {
                return false;
            }

            List<ShopItem> items = this.shopItems.get(selectedBigCategory).remove(this.selectedCategory);

            this.addAtIndex(this.shopItems.get(selectedBigCategory), this.selectedCategory, items, categoryIndex - 1);
            return true;
        } else if (pKeyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.getFocused() instanceof Widget widget && this.renderables.contains(widget)) {
                return false;
            }

            int categoryIndex = this.shopItems.get(selectedBigCategory).keySet().stream().toList().indexOf(this.selectedCategory);

            if (categoryIndex == -1 || categoryIndex == this.shopItems.size() - 1) {
                return false;
            }

            List<ShopItem> items = this.shopItems.get(selectedBigCategory).remove(this.selectedCategory);

            this.addAtIndex(this.shopItems.get(selectedBigCategory), this.selectedCategory, items, categoryIndex + 1);
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    @Override
    public void onClose() {
        this.sendChanges();
        super.onClose();
    }
}
