package me.khajiitos.jackseconomy.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.price.ItemDescription;
import me.khajiitos.jackseconomy.screen.widget.BetterScrollPanel;
import me.khajiitos.jackseconomy.screen.widget.CategoryEntry;
import me.khajiitos.jackseconomy.screen.widget.EditCategoryEntry;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class AdminShopScreen extends AbstractContainerScreen<AdminShopMenu> {
    protected static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/admin_shop.png");
    protected static final ResourceLocation NO_WALLET = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/no_wallet.png");
    protected static final ResourceLocation BALANCE_PROGRESS = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");
    protected final LinkedHashMap<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> shopItems = new LinkedHashMap<>() {
        @Override
        public LinkedHashMap<InnerCategory, List<ShopItem>> get(Object key) {
            return this.getOrDefault(key, new LinkedHashMap<>());
        }
    };
    public LinkedHashMap<ShopItem, Integer> shoppingCart = new LinkedHashMap<>();
    protected InnerCategory selectedCategory = null;
    protected Category selectedBigCategory = null;
    protected int categoryOffset = 0;
    protected int page = 0;
    protected List<Component> tooltip;

    protected ImageButton categoryPreviousButton;
    protected ImageButton categoryNextButton;
    protected ImageButton pageNextButton;
    protected ImageButton pagePreviousButton;

    protected boolean shouldRenderBackground;

    protected BetterScrollPanel categoryPanel;

    public AdminShopScreen(AdminShopMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

        CompoundTag data = pMenu.data;

        ListTag categoriesTag = data.getList("categories", Tag.TAG_COMPOUND);
        ListTag itemsTag = data.getList("items", Tag.TAG_COMPOUND);

        categoriesTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                String categoryName = compoundTag.getString("name");
                Item item = ItemHelper.getItem(compoundTag.getString("item"));

                if (item == null) {
                    return;
                }

                Category category = new Category(categoryName, item);
                LinkedHashMap<InnerCategory, List<ShopItem>> innerCategories = new LinkedHashMap<>();
                shopItems.put(category, innerCategories);
                ListTag categoriesInnerTag = compoundTag.getList("categories", Tag.TAG_COMPOUND);

                categoriesInnerTag.forEach(innerTag -> {
                    if (innerTag instanceof CompoundTag innerCompoundTag) {
                        String innerCategoryName = innerCompoundTag.getString("name");
                        Item innerItem = ItemHelper.getItem(innerCompoundTag.getString("item"));

                        if (innerItem == null) {
                            return;
                        }

                        InnerCategory innerCategory = new InnerCategory(innerCategoryName, item);
                        innerCategories.put(innerCategory, new ArrayList<>());

                        if (selectedCategory == null) {
                            selectedCategory = innerCategory;
                        }
                    }
                });

                if (selectedBigCategory == null && !innerCategories.isEmpty()) {
                    selectedBigCategory = category;
                }
            }
        });

        LinkedHashMap<String, List<UnpreparedShopItem>> slotlessShopItems = new LinkedHashMap<>();

        itemsTag.forEach(tag -> {
            if (tag instanceof CompoundTag compoundTag) {
                ItemDescription itemDescription = ItemDescription.fromNbt(compoundTag);

                if (itemDescription == null) {
                    return;
                }

                String category = compoundTag.getString("category");
                double buyPrice = compoundTag.getDouble("adminShopBuyPrice");
                int slot = compoundTag.contains("slot") ? compoundTag.getInt("slot") : -1;
                String customName = compoundTag.contains("customAdminShopName") ? compoundTag.getString("customAdminShopName") : null;

                if (slot < 0) {
                    slotlessShopItems.computeIfAbsent(category, categoryName -> new ArrayList<>()).add(new UnpreparedShopItem(itemDescription, buyPrice, customName));
                } else {
                    String[] categoryNamesInner = category.split(":", 2);
                    if (categoryNamesInner.length < 2) {
                        return;
                    }

                    String bigCategoryName = categoryNamesInner[0];
                    String innerCategoryName = categoryNamesInner[1];

                    for (Map.Entry<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> categoryEntry : shopItems.entrySet()) {
                        if (categoryEntry.getKey().name.equals(bigCategoryName)) {
                            for (Map.Entry<InnerCategory, List<ShopItem>> entry : shopItems.get(categoryEntry.getKey()).entrySet()) {
                                if (entry.getKey().name.equals(innerCategoryName)) {
                                    entry.getValue().add(new ShopItem(itemDescription, buyPrice, slot, customName));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

            }
        });

        slotlessShopItems.forEach((categoryName, list) -> {
            String[] categoriesNames = categoryName.split(":", 2);

            if (categoriesNames.length < 2) {
                return;
            }

            String bigCategoryName = categoriesNames[0];
            String innerCategoryName = categoriesNames[1];
            
            for (Map.Entry<Category, LinkedHashMap<InnerCategory, List<ShopItem>>> entry : shopItems.entrySet()) {
                if (entry.getKey().name.equals(bigCategoryName)) {
                    for (UnpreparedShopItem item : list) {
                        for (Map.Entry<InnerCategory, List<ShopItem>> entry1 : entry.getValue().entrySet()) {
                            if (entry1.getKey().name.equals(innerCategoryName)) {
                                shopItems.get(entry.getKey()).get(entry1.getKey()).add(new ShopItem(item.itemDescription, item.price, this.findFirstAvailableSlot(entry.getKey()), item.customName));
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        });

        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private void initButtons() {
        if (this.categoryPreviousButton != null) {
            this.removeWidget(this.categoryPreviousButton);
            this.categoryPreviousButton = null;
        }

        if (this.categoryNextButton != null) {
            this.removeWidget(this.categoryNextButton);
            this.categoryNextButton = null;
        }

        if (this.pagePreviousButton != null) {
            this.removeWidget(this.pagePreviousButton);
            this.pagePreviousButton = null;
        }

        if (this.pageNextButton != null) {
            this.removeWidget(this.pageNextButton);
            this.pageNextButton = null;
        }

        if (this.categoryOffset > 0) {
            this.categoryPreviousButton = this.addRenderableWidget(new ImageButton(this.leftPos + 6, this.topPos + 30, 6, 10, 176, 58, 10, BACKGROUND, (b) -> {
                this.categoryOffset--;
                this.initButtons();
            }));
        }

        if (this.categoryOffset + (this.isEditMode() ? 7 : 8) < this.shopItems.size()) {
            this.categoryNextButton = this.addRenderableWidget(new ImageButton(this.leftPos + 164, this.topPos + 30, 6, 10, 182, 58, 10, BACKGROUND, (b) -> {
                this.categoryOffset++;
                this.initButtons();
            }));
        }

        if (this.page > 0) {
            this.pagePreviousButton = this.addRenderableWidget(new ImageButton(this.leftPos + 55, this.topPos + 126, 6, 10, 176, 58, 10, BACKGROUND, (b) -> {
                this.page--;
                this.initButtons();
            }));
        }

        if (this.isEditMode() || this.page < this.getPages(this.selectedCategory) - 1) {
            this.pageNextButton = this.addRenderableWidget(new ImageButton(this.leftPos + 113, this.topPos + 126, 6, 10, 182, 58, 10, BACKGROUND, (b) -> {
                this.page++;
                this.initButtons();
            }));
        }
    }

    protected <A, B> void addAtIndex(LinkedHashMap<A, B> hashMap, A key, B value, int index) {
        if (index == hashMap.size()) {
            hashMap.put(key, value);
            return;
        }

        LinkedHashMap<A, B> copy = new LinkedHashMap<>(hashMap);

        hashMap.clear();

        int i = 0;

        for (Map.Entry<A, B> entry : copy.entrySet()) {
            if (i == index) {
                hashMap.put(key, value);
            }

            hashMap.put(entry.getKey(), entry.getValue());
            i++;
        }
    }

    protected int getPages(InnerCategory selectedCategory) {
        if (selectedCategory == null) {
            return 0;
        }

        List<ShopItem> itemsList = this.shopItems.get(selectedBigCategory).get(selectedCategory);

        int pages = 1;

        for (ShopItem shopItem : itemsList) {
            int onPage = 1 + shopItem.slot() / 27;
            if (pages < onPage) {
                pages = onPage;
            }
        }

        return pages;
    }

    public BigDecimal getShoppingCartValue() {
        BigDecimal value = BigDecimal.ZERO;
        for (Map.Entry<ShopItem, Integer> items : this.shoppingCart.entrySet()) {
            value = value.add(BigDecimal.valueOf(items.getKey().price()).multiply(new BigDecimal(items.getValue())));
        }
        return value;
    }

    protected @Nullable InnerCategory getCategoryById(int id) {
        List<InnerCategory> categories = this.shopItems.get(selectedBigCategory).keySet().stream().toList();

        if (id < 0 || id >= categories.size()) {
            return null;
        }

        return categories.get(id);
    }

    protected void selectBigCategory(Category bigCategory) {
        // TODO: make sure this is actually a big category, otherwise we will crash lol
        this.selectedBigCategory = bigCategory;
        this.selectedCategory = null;

        for (InnerCategory innerCategory : this.shopItems.get(bigCategory).keySet()) {
            this.selectedCategory = innerCategory;
            break;
        }

        this.page = 0;
        this.initButtons();
    }

    protected void initCategoryPanel() {
        this.categoryPanel = this.addRenderableWidget(new BetterScrollPanel(Minecraft.getInstance(), this.leftPos - 80, this.topPos + 20, 75, this.imageHeight - 40));

        for (Category category : shopItems.keySet()) {
            this.categoryPanel.children.add(new CategoryEntry(0, 0, 75, 25, category, (entry, button) -> selectBigCategory(category)));
        }
    }

    @Override
    protected void init() {
        super.init();
        initButtons();
        initCategoryPanel();

        if (!this.isEditMode()) {
            this.addRenderableWidget(new ImageButton(this.leftPos + 139, this.topPos + 121, 30, 26, 176, 0, 26, BACKGROUND, 256, 256, (b) -> {
                assert this.minecraft != null;
                this.minecraft.screen = new ShoppingCartScreen(this.menu, this.menu.inventory, this);
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());

            }, (a, b, c, d) -> {
                this.tooltip = List.of(Component.translatable("jackseconomy.items", Component.literal(String.valueOf(this.shoppingCart.size()))).withStyle(ChatFormatting.GRAY),
                        Component.translatable("jackseconomy.value", Component.literal(CurrencyHelper.format(getShoppingCartValue()))).withStyle(ChatFormatting.GRAY));
            }, Component.empty()));

            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null && player.isCreative() && player.getPermissionLevel() >= 4) {
                this.addRenderableWidget(new Button(3, 3, 75, 20, Component.translatable("jackseconomy.edit"), (b) -> {
                    Minecraft.getInstance().screen = new EditAdminShopScreen(this.menu, this.menu.inventory, this.title);
                    Minecraft.getInstance().screen.init(Minecraft.getInstance(), this.width, this.height);
                }));
            }
        }
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        if (!this.shouldRenderBackground) {
            return;
        }

        this.renderBackground(pPoseStack);

        RenderSystem.setShaderTexture(0, BACKGROUND);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void addItemToCart(ShopItem shopItem, int amount) {
        int countAlready = shoppingCart.getOrDefault(shopItem, 0);

        int newCount = Math.max(0, countAlready + amount);

        if (newCount == 0) {
            shoppingCart.remove(shopItem);
        } else {
            shoppingCart.put(shopItem, newCount);
        }
    }

    protected boolean isHoverObstructed(int mouseX, int mouseY) {
        return false;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        tooltip = null;

        shouldRenderBackground = true;
        this.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);

        if (this.selectedCategory != null) {
            GuiComponent.drawCenteredString(pPoseStack, this.font, this.selectedCategory.name, this.leftPos + (this.imageWidth / 2), this.topPos + 6, 0xFFFFFFFF);
        }

        List<InnerCategory> categories = this.shopItems.get(selectedBigCategory).keySet().stream().toList();
        for (int i = 0; i < 8; i++) {
            int categoryId = this.categoryOffset + i;
            int categoryX = this.leftPos + 17 + i * 18, categoryY = this.topPos + 27;
            boolean hovered = pMouseX >= categoryX && pMouseX <= categoryX + 16 && pMouseY >= categoryY && pMouseY <= categoryY + 16;

            if (hovered && !isHoverObstructed(pMouseX, pMouseY)) {
                renderSlotHighlight(pPoseStack, categoryX, categoryY, this.getBlitOffset());
            }

            if (categoryId >= 0 && categoryId < categories.size()) {
                InnerCategory category = categories.get(categoryId);
                Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(category.item), categoryX, categoryY);

                if (hovered) {
                    if (this.isEditMode()) {
                        this.tooltip = List.of(Component.literal(category.name), Component.translatable("jackseconomy.right_click_to_rename").withStyle(ChatFormatting.GRAY), Component.translatable("jackseconomy.middle_click_to_remove_category").withStyle(ChatFormatting.RED));
                    } else {
                        this.tooltip = List.of(Component.literal(category.name));
                    }
                }

                if (this.selectedCategory == category) {
                    RenderSystem.setShaderTexture(0, BACKGROUND);
                    this.blit(pPoseStack, categoryX + 4, categoryY + 19, 176, 52, 10, 6);
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = this.leftPos + 8 + col * 18, slotY = this.topPos + 66 + row * 18;

                ShopItem shopItem = this.getItemAtSlot(this.page * 27 + row * 9 + col, this.selectedCategory);

                if (shopItem != null) {
                    ItemStack itemStack = shopItem.itemDescription.createItemStack();
                    Minecraft.getInstance().getItemRenderer().renderGuiItem(itemStack, slotX, slotY);
                }

                if (pMouseX >= slotX && pMouseX <= slotX + 16 && pMouseY >= slotY && pMouseY <= slotY + 16 && !isHoverObstructed(pMouseX, pMouseY)) {
                    renderSlotHighlight(pPoseStack, slotX, slotY, this.getBlitOffset());

                    if (shopItem != null) {
                        ItemStack itemStack = shopItem.itemDescription().createItemStack();
                        this.tooltip = new ArrayList<>();

                        this.tooltip.add(shopItem.customName != null ? Component.literal(shopItem.customName) : shopItem.itemDescription.item().getDescription().copy().withStyle(shopItem.itemDescription.item().getRarity(itemStack).getStyleModifier()));

                        Level level = Minecraft.getInstance().player == null ? null : Minecraft.getInstance().player.level;
                        itemStack.getItem().appendHoverText(itemStack, level, this.tooltip, TooltipFlag.Default.NORMAL);

                        this.tooltip.add(Component.literal(" "));

                        if (this.isEditMode()) {
                            this.tooltip.add(shopItem.price == -1 ? Component.translatable("jackseconomy.no_price").withStyle(ChatFormatting.GRAY) : Component.literal(CurrencyHelper.format(shopItem.price)).withStyle(ChatFormatting.GRAY));
                            this.tooltip.add(Component.translatable("jackseconomy.right_click_to_edit_price").withStyle(ChatFormatting.AQUA));
                            this.tooltip.add(Component.translatable("jackseconomy.shift_right_click_to_rename").withStyle(ChatFormatting.AQUA));
                            this.tooltip.add(Component.translatable("jackseconomy.middle_click_to_remove").withStyle(ChatFormatting.RED));
                        } else {
                            this.tooltip.add(Component.literal(CurrencyHelper.format(shopItem.price)).withStyle(ChatFormatting.GRAY));
                            this.tooltip.add(Component.translatable("jackseconomy.in_cart", Component.literal(String.valueOf(shoppingCart.getOrDefault(shopItem, 0))).withStyle(ChatFormatting.AQUA)));
                        }
                    }
                }
            }
        }

        if (!this.isEditMode()) {
            ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);

            if (wallet != null && wallet.getItem() instanceof WalletItem walletItem) {
                BigDecimal balance = WalletItem.getBalance(wallet);

                Component component = Component.literal(CurrencyHelper.format(balance));
                int textWidth = this.font.width(component);
                int totalWidth = 29 + textWidth;
                GuiComponent.fill(pPoseStack, this.leftPos + 181, this.topPos + 5, this.leftPos + 181 + totalWidth, this.topPos + 35, 0xFF4c4c4c);
                GuiComponent.fill(pPoseStack, this.leftPos + 182, this.topPos + 6, this.leftPos + 182 + totalWidth, this.topPos + 34, 0xFFc6c6c6);
                Minecraft.getInstance().getItemRenderer().renderGuiItem(wallet, this.leftPos + 183, this.topPos + 8);
                this.font.draw(pPoseStack, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF);

                RenderSystem.setShaderTexture(0, BALANCE_PROGRESS);

                double progress = balance.divide(BigDecimal.valueOf(walletItem.getCapacity()), RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();
                blit(pPoseStack, this.leftPos + 181 + ((totalWidth - 51) / 2), this.topPos + 26, this.getBlitOffset(), 0, 0, 51, 5, 256, 256);
                blit(pPoseStack, this.leftPos + 181 + ((totalWidth - 51) / 2), this.topPos + 26, this.getBlitOffset(), 0, 5, ((int)(51 * progress)), 5, 256, 256);
            } else {
                Component component = Component.translatable("jackseconomy.no_wallet").withStyle(ChatFormatting.DARK_RED);
                int width = this.font.width(component);
                GuiComponent.fill(pPoseStack, this.leftPos + 181, this.topPos + 5, this.leftPos + 209 + width, this.topPos + 25, 0xFF4c4c4c);
                GuiComponent.fill(pPoseStack, this.leftPos + 182, this.topPos + 6, this.leftPos + 208 + width, this.topPos + 24, 0xFFc6c6c6);
                RenderSystem.setShaderTexture(0, NO_WALLET);
                blit(pPoseStack, this.leftPos + 183, this.topPos + 8, this.getBlitOffset(), 0, 0, 16, 16, 16, 16);
                this.font.draw(pPoseStack, component, this.leftPos + 203, this.topPos + 13, 0xFFFFFFFF);
            }
        }

        this.shouldRenderBackground = false;
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderStageTwo(pPoseStack, pMouseX, pMouseY, pPartialTick);

        this.renderTooltip(pPoseStack, pMouseX, pMouseY);

        if (tooltip != null) {
            this.renderTooltip(pPoseStack, tooltip, Optional.empty(), pMouseX, pMouseY);
        }
    }

    protected void renderStageTwo(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {}

    protected boolean onCategorySlotClicked(int pButton, int categoryId) {
        if (pButton == 0) {
            List<InnerCategory> categories = this.shopItems.get(selectedBigCategory).keySet().stream().toList();

            if (categoryId >= 0 && categoryId < categories.size()) {
                this.selectedCategory = categories.get(categoryId);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.25F));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (int i = 0; i < 8; i++) {
            int categoryId = this.categoryOffset + i;
            int categoryX = this.leftPos + 17 + i * 18, categoryY = this.topPos + 27;

            if (pMouseX >= categoryX && pMouseX <= categoryX + 16 && pMouseY >= categoryY && pMouseY <= categoryY + 16) {
                if (onCategorySlotClicked(pButton, categoryId)) {
                    return true;
                }
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = this.leftPos + 8 + col * 18, slotY = this.topPos + 66 + row * 18;

                if (pMouseX >= slotX && pMouseX <= slotX + 16 && pMouseY >= slotY && pMouseY <= slotY + 16) {
                    this.onSlotClicked(this.page * 27 + row * 9 + col, pButton);
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    protected @Nullable ShopItem getItemAtSlot(int slot, InnerCategory category) {
        for (ShopItem shopItem : this.shopItems.get(selectedBigCategory).getOrDefault(category, List.of())) {
            if (shopItem.slot == slot) {
                return shopItem;
            }
        }

        return null;
    }

    // Returns old item
    protected ShopItem setItemAtSlot(ShopItem shopItem, int slot, InnerCategory category) {
        ShopItem existingItem = getItemAtSlot(slot, category);

        if (existingItem != null) {
            this.shopItems.get(selectedBigCategory).getOrDefault(category, List.of()).remove(existingItem);
        }

        if (shopItem != null) {
            this.shopItems.get(selectedBigCategory).getOrDefault(category, List.of()).add(new ShopItem(shopItem.itemDescription, shopItem.price, slot, shopItem.customName));
        }

        return existingItem;
    }

    protected void onSlotClicked(int slot, int pButton) {
        if (pButton != 0 && pButton != 1) {
            return;
        }

        ShopItem shopItem = getItemAtSlot(slot, this.selectedCategory);

        if (shopItem == null) {
            return;
        }

        int count;
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            count = 64;
        } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            count = 10;
        } else {
            count = 1;
        }

        if (pButton == 1) {
            count *= -1;
        }

        this.addItemToCart(shopItem, count);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pButton == 1 ? 0.75F : 1.25F));
    }

    protected int findFirstAvailableSlot(InnerCategory category) {
        for (int slot = 0;;slot++) {
            ShopItem existingShopItem = this.getItemAtSlot(slot, category);

            if (existingShopItem == null) {
                return slot;
            }
        }
    }

    protected Pair<Integer, Integer> getSlotPos(int slot) {
        int slotInPage = slot % 27;
        int row = slotInPage / 9;
        int col = slotInPage % 9;
        return Pair.of(this.leftPos + 8 + col * 18, this.topPos + 66 + row * 18);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;

        if (!this.shoppingCart.isEmpty()) {
            this.minecraft.screen = new AdminShopExitPromptScreen(this);
            this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
        } else {
            super.onClose();
        }
    }

    protected boolean isEditMode() {
        return false;
    }

    public static class InnerCategory {
        protected String name;
        protected Item item;

        public InnerCategory(String name, Item item) {
            this.name = name;
            this.item = item;
        }

        public String getName() {
            return name;
        }

        public Item getItem() {
            return item;
        }
    }

    public static class Category extends InnerCategory {
        public Category(String name, Item item) {
            super(name, item);
        }
    }

    public record ShopItem(ItemDescription itemDescription, double price, int slot, String customName) { }
    private record UnpreparedShopItem(ItemDescription itemDescription, double price, String customName) { }
}