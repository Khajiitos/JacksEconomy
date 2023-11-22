package me.khajiitos.jackseconomy.price;

import com.google.gson.*;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import me.khajiitos.jackseconomy.util.ItemHelper;
import me.khajiitos.jackseconomy.util.NewShopUnlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ItemPriceManager {
    //private static final LinkedHashMap<ItemDescription, ItemPriceInfo> itemPriceInfos = new LinkedHashMap<>();
    private static final List<ItemPriceEntry> itemPriceInfos = new ArrayList<>();
    private static final LinkedHashMap<Category, List<Category>> categories = new LinkedHashMap<>();
    private static final File file = new File("config/jackseconomy_prices.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        itemPriceInfos.add(new ItemPriceEntry(new ItemDescription(Items.DIAMOND, null), new PricesItemPriceInfo(50.0, 45.0,100.0, null)));
        itemPriceInfos.add(new ItemPriceEntry(new ItemDescription(Items.DIAMOND, null), new AdminShopItemPriceInfo(150.0, "General:Gems", 0, null, null)));

        ArrayList<Category> categoriesInnerDefault = new ArrayList<>();
        categoriesInnerDefault.add(new Category("Gems", Items.DIAMOND));
        categories.put(new Category("General", Items.DIRT), categoriesInnerDefault);
    }

    @Deprecated
    public static ItemPriceInfo getInfo(ItemDescription itemDescription) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription)).findFirst().map(ItemPriceEntry::itemPriceInfo).orElse(null);
    }

    public static PricesItemPriceInfo getPricesInfo(ItemDescription itemDescription) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo)).findFirst().orElse(null);
    }

    public static ItemPriceInfo getInfo(ItemStack itemStack) {
        return getInfo(ItemDescription.ofItem(itemStack));
    }

    public static List<ItemPriceEntry> getItemPriceInfos() {
        return itemPriceInfos;
    }

    public static LinkedHashMap<Category, List<Category>> getCategories() {
        return categories;
    }

    public static double getExporterSellPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).sellPrice * count).findFirst().orElse(-1.0);
    }

    public static double getImporterBuyPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).importerBuyPrice * count).findFirst().orElse(-1.0);
    }

    public static double getAdminShopSellPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo).map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).adminShopSellPrice * count).findFirst().orElse(-1.0);
    }

    public static String getAdminShopSellStage(ItemDescription itemDescription) {
        return itemPriceInfos.stream()
                .filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof PricesItemPriceInfo)
                .map(entry -> ((PricesItemPriceInfo)entry.itemPriceInfo).adminShopSellStage)
                .map(Optional::ofNullable)
                .findFirst()
                .orElse(Optional.empty()).orElse(null);
    }

    public static double getAdminShopBuyPrice(ItemDescription itemDescription, int count, int slot, String category) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription) && itemPriceEntry.itemPriceInfo instanceof AdminShopItemPriceInfo adminShopItemPriceInfo && adminShopItemPriceInfo.adminShopSlot == slot && Objects.equals(adminShopItemPriceInfo.category, category)).map(entry -> ((AdminShopItemPriceInfo)entry.itemPriceInfo).adminShopBuyPrice * count).findFirst().orElse(-1.0);
    }

    public static void load() {
        if (file.exists()) {
            itemPriceInfos.clear();
            categories.clear();

            try (FileReader fileReader = new FileReader(file)) {
                JsonObject pricesObj = GSON.fromJson(fileReader, JsonObject.class);
                JsonArray itemsArray = pricesObj.getAsJsonArray("items");
                JsonArray categoriesArray = pricesObj.getAsJsonArray("categories");

                if (itemsArray == null || categoriesArray == null) {
                    JacksEconomy.LOGGER.error("Invalid jackseconomy_prices.json file");
                    save();
                    return;
                }

                categoriesArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();
                    String categoryName = object.get("name").getAsString();
                    String itemName = object.get("item").getAsString();

                    if (itemName == null) {
                        return;
                    }

                    Item item = ItemHelper.getItem(itemName);

                    if (item == null) {
                        return;
                    }

                    JsonArray categoriesList = object.getAsJsonArray("categories");

                    if (categoriesList != null) {
                        Category category = new Category(categoryName, item);
                        ArrayList<Category> innerCategories = new ArrayList<>();
                        categories.put(category, innerCategories);

                        categoriesList.forEach(jsonElementInner -> {
                            if (jsonElementInner instanceof JsonObject categoryObject) {
                                String categoryNameInner = categoryObject.get("name").getAsString();
                                String itemNameInner = categoryObject.get("item").getAsString();

                                if (itemNameInner == null) {
                                    return;
                                }

                                Item itemInner = ItemHelper.getItem(itemNameInner);

                                if (itemInner == null) {
                                    return;
                                }

                                innerCategories.add(new Category(categoryNameInner, itemInner));
                            }
                        });
                    }
                });

                itemsArray.forEach(jsonElement -> {
                    JsonObject object = jsonElement.getAsJsonObject();

                    ItemDescription itemDescription = ItemDescription.fromJson(object);

                    if (itemDescription != null) {
                        ItemPriceInfo[] priceInfos = ItemPriceInfo.fromJson(object);
                        for (ItemPriceInfo priceInfo : priceInfos) {
                            itemPriceInfos.add(new ItemPriceEntry(itemDescription, priceInfo));
                        }
                    } else {
                        JacksEconomy.LOGGER.warn("Invalid price info");
                    }
                });
            } catch (JsonSyntaxException | ClassCastException | IOException e) {
                JacksEconomy.LOGGER.error("Failed to load item prices", e);
            }
        } else if (file.getParentFile().isDirectory() || file.getParentFile().mkdirs()) {
            save();
        }
    }

    public static void save() {
        try (FileWriter fileWriter = new FileWriter(file)) {
            JsonObject object = new JsonObject();
            JsonArray itemsArray = new JsonArray();
            JsonArray categoriesArray = new JsonArray();

            itemPriceInfos.forEach((entry) -> {
                itemsArray.add(merge(entry.itemDescription.toJson(), entry.itemPriceInfo.toJson()));
            });

            categories.forEach((category, categories) -> {
                String itemName = ItemHelper.getItemName(category.icon);

                if (itemName != null) {
                    JsonObject categoryObj = new JsonObject();
                    categoryObj.addProperty("item", itemName);
                    categoryObj.addProperty("name", category.name);

                    JsonArray innerCategories = new JsonArray();

                    categories.forEach(categoryInner -> {
                        JsonObject categoryInnerObj = new JsonObject();

                        String itemNameInner = ItemHelper.getItemName(categoryInner.icon);

                        if (itemNameInner != null) {
                            categoryInnerObj.addProperty("item", itemNameInner);
                            categoryInnerObj.addProperty("name", categoryInner.name);
                            innerCategories.add(categoryInnerObj);
                        }
                    });

                    categoryObj.add("categories", innerCategories);
                    categoriesArray.add(categoryObj);
                }
            });

            object.add("items", itemsArray);
            object.add("categories", categoriesArray);

            fileWriter.write(GSON.toJson(object));
        } catch (IOException e) {
            JacksEconomy.LOGGER.error("Failed to save item prices", e);
        }
    }

    public static ListTag toTag() {
        ListTag listTag = new ListTag();
        itemPriceInfos.forEach((entry) -> {
            if (entry.itemPriceInfo instanceof PricesItemPriceInfo itemPriceInfo) {
                CompoundTag itemTag = entry.itemDescription.toNbt().copy();
                itemTag.putDouble("sellPrice", itemPriceInfo.sellPrice);
                itemTag.putDouble("importerBuyPrice", itemPriceInfo.importerBuyPrice);
                itemTag.putDouble("adminShopSellPrice", itemPriceInfo.adminShopSellPrice);
                if (itemPriceInfo.adminShopSellStage != null) {
                    itemTag.putString("adminShopSellStage", itemPriceInfo.adminShopSellStage);
                }
                listTag.add(itemTag);
            }
        });
        return listTag;
    }

    private static int getPagesCount(String categoryName) {
        int maxPage = 1;
        for (ItemPriceEntry entry : itemPriceInfos) {
            if (entry.itemPriceInfo instanceof AdminShopItemPriceInfo adminShopItemPriceInfo) {
                if (Objects.equals(adminShopItemPriceInfo.category, categoryName)) {
                    int itemPage = 1 + adminShopItemPriceInfo.adminShopSlot / 27;

                    if (itemPage > maxPage) {
                        maxPage = itemPage;
                    }
                }
            }
        }
        return maxPage;
    }

    public static CompoundTag toAdminShopSchemaCompound(Player player) {
        CompoundTag tag = new CompoundTag();

        ListTag itemsTag = new ListTag();
        ListTag categoriesTag = new ListTag();

        NewShopUnlocks shopUnlocks = GameStagesManager.getNewShopUnlocks(player);

        itemPriceInfos.forEach((entry) -> {
            if (entry.itemPriceInfo instanceof AdminShopItemPriceInfo itemPriceInfo) {
                if (itemPriceInfo.adminShopBuyPrice <= 0) {
                    return;
                }

                CompoundTag itemTag = entry.itemDescription.toNbt();
                itemTag.putDouble("adminShopBuyPrice", itemPriceInfo.adminShopBuyPrice);
                itemTag.putString("category", itemPriceInfo.category);
                itemTag.putInt("slot", itemPriceInfo.adminShopSlot);

                if (itemPriceInfo.customAdminShopName != null) {
                    itemTag.putString("customAdminShopName", itemPriceInfo.customAdminShopName);
                }

                if (itemPriceInfo.adminShopStage != null) {
                    itemTag.putString("adminShopStage", itemPriceInfo.adminShopStage);
                }

                if (shopUnlocks != null && shopUnlocks.unlockedItems.contains(new NewShopUnlocks.Item(itemPriceInfo.adminShopSlot, itemPriceInfo.category))) {
                    itemTag.putBoolean("recentlyUnlocked", true);
                }

                itemsTag.add(itemTag);
            } else if (entry.itemPriceInfo instanceof PricesItemPriceInfo itemPriceInfo) {
                if (itemPriceInfo.adminShopSellPrice <= 0) {
                    return;
                }

                CompoundTag itemTag = entry.itemDescription.toNbt();

                itemTag.putDouble("adminShopSellPrice", itemPriceInfo.adminShopSellPrice);

                if (itemPriceInfo.adminShopSellStage != null) {
                    itemTag.putString("adminShopSellStage", itemPriceInfo.adminShopSellStage);
                }

                itemsTag.add(itemTag);
            }
        });

        categories.forEach((category, categories) -> {
            CompoundTag compoundTag = new CompoundTag();

            String itemName = ItemHelper.getItemName(category.icon);

            if (itemName == null) {
                return;
            }

            compoundTag.putString("name", category.name);
            compoundTag.putString("item", itemName);

            if (shopUnlocks != null && shopUnlocks.unlockedCategories.contains(category.name)) {
                compoundTag.putBoolean("recentlyUnlocked", true);
            }

            ListTag innerCategories = new ListTag();

            categories.forEach(categoryInner -> {
                CompoundTag compoundTagInner = new CompoundTag();

                String itemNameInner = ItemHelper.getItemName(categoryInner.icon);

                if (itemNameInner == null) {
                    return;
                }

                compoundTagInner.putString("name", categoryInner.name);
                compoundTagInner.putString("item", itemNameInner);

                if (shopUnlocks != null && shopUnlocks.unlockedCategories.contains(category.name + ":" + categoryInner.name)) {
                    compoundTagInner.putBoolean("recentlyUnlocked", true);
                }

                innerCategories.add(compoundTagInner);
            });

            compoundTag.put("categories", innerCategories);
            categoriesTag.add(compoundTag);
        });

        tag.put("items", itemsTag);
        tag.put("categories", categoriesTag);

        return tag;
    }

    // TODO: merge if exists?
    public static void addPriceInfo(ItemDescription itemDescription, ItemPriceInfo priceInfo) {
        itemPriceInfos.add(new ItemPriceEntry(itemDescription, priceInfo));
    }

    public static void addPriceInfo(ItemStack itemStack, ItemPriceInfo priceInfo) {
        addPriceInfo(ItemDescription.ofItem(itemStack), priceInfo);
    }

    public static void sendDataToPlayers() {
        JacksEconomy.server.getPlayerList().getPlayers().forEach(serverPlayer -> Packets.sendToClient(serverPlayer, new PricesInfoPacket(ItemPriceManager.toTag())));
    }

    private static JsonObject merge(JsonObject object1, JsonObject object2) {
        JsonObject object = new JsonObject();
        object1.keySet().forEach(name -> object.add(name, object1.get(name)));
        object2.keySet().forEach(name -> object.add(name, object2.get(name)));
        return object;
    }

    public record Category(String name, Item icon) {}
    public record ItemPriceEntry(ItemDescription itemDescription, ItemPriceInfo itemPriceInfo) {}
}
