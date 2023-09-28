package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.UpdateAdminShopPacket;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.FloatingEditBoxWidget;
import me.khajiitos.jackseconomy.util.ItemHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditAdminShopScreen extends AdminShopScreen {
    protected ShopItem itemOnCursor;
    protected FloatingEditBoxWidget floatingEditBox;

    public EditAdminShopScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, null);

        pMenu.setSlotsLocked(true);
    }

    @Override
    protected boolean isHoverObstructed(int mouseX, int mouseY) {
        return this.floatingEditBox != null && mouseX >= this.floatingEditBox.x && mouseX <= this.floatingEditBox.x + this.floatingEditBox.getWidth() && mouseY >= this.floatingEditBox.y && mouseY <= this.floatingEditBox.y + this.floatingEditBox.getWidth();
    }

    protected String getUnnamedCategoryName() {
        List<InnerCategory> categories = this.shopItems.keySet().stream().toList();
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
            List<InnerCategory> categories = this.shopItems.keySet().stream().toList();

            if (categoryId == categories.size() && this.itemOnCursor != null && this.floatingEditBox == null) {
                InnerCategory category = new InnerCategory(this.getUnnamedCategoryName(), this.itemOnCursor.itemDescription().item());
                this.shopItems.put(category, new ArrayList<>());

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
                this.shopItems.remove(category);

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

        this.shopItems.forEach((category, itemList) -> {
            String itemName = ItemHelper.getItemName(category.item);

            if (itemName == null) {
                return;
            }

            CompoundTag categoryTag = new CompoundTag();
            categoryTag.putString("name", category.name);
            categoryTag.putString("item", itemName);

            categoriesTag.add(categoryTag);

            for (ShopItem shopItem : itemList) {
                CompoundTag itemTag = shopItem.itemDescription().toNbt();
                itemTag.putDouble("adminShopBuyPrice", shopItem.price());
                itemTag.putString("category", category.name);
                itemTag.putInt("slot", shopItem.slot());

                if (shopItem.customName() != null) {
                    itemTag.putString("customAdminShopName", shopItem.customName());
                }

                itemsTag.add(itemTag);
            }
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

            int categoryIndex = this.shopItems.keySet().stream().toList().indexOf(this.selectedCategory);

            if (categoryIndex == -1 || categoryIndex == 0) {
                return false;
            }

            List<ShopItem> items = this.shopItems.remove(this.selectedCategory);

            this.addAtIndex(this.shopItems, this.selectedCategory, items, categoryIndex - 1);
            return true;
        } else if (pKeyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.getFocused() instanceof Widget widget && this.renderables.contains(widget)) {
                return false;
            }

            int categoryIndex = this.shopItems.keySet().stream().toList().indexOf(this.selectedCategory);

            if (categoryIndex == -1 || categoryIndex == this.shopItems.size() - 1) {
                return false;
            }

            List<ShopItem> items = this.shopItems.remove(this.selectedCategory);

            this.addAtIndex(this.shopItems, this.selectedCategory, items, categoryIndex + 1);
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
