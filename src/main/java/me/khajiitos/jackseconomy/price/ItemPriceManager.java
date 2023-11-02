package me.khajiitos.jackseconomy.price;

import com.google.gson.*;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import me.khajiitos.jackseconomy.util.ItemHelper;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ItemPriceManager {
    //private static final LinkedHashMap<ItemDescription, ItemPriceInfo> itemPriceInfos = new LinkedHashMap<>();
    private static final List<ItemPriceEntry> itemPriceInfos = new ArrayList<>();
    private static final LinkedHashMap<Category, List<Category>> categories = new LinkedHashMap<>();
    private static final File file = new File("config/jackseconomy_prices.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        itemPriceInfos.add(new ItemPriceEntry(new ItemDescription(Items.DIAMOND, null), new ItemPriceInfo(50.0, 45.0,100.0, 150.0, "General:Gems", 0, null, null)));

        ArrayList<Category> categoriesInnerDefault = new ArrayList<>();
        categoriesInnerDefault.add(new Category("Gems", Items.DIAMOND));
        categories.put(new Category("General", Items.DIRT), categoriesInnerDefault);
    }

    @Deprecated
    public static ItemPriceInfo getInfo(ItemDescription itemDescription) {
        return itemPriceInfos.stream().filter(itemPriceEntry -> itemPriceEntry.itemDescription.equals(itemDescription)).findFirst().map(ItemPriceEntry::itemPriceInfo).orElse(null);
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
        return itemPriceInfos.stream().filter(entry -> entry.itemDescription.equals(itemDescription) && entry.itemPriceInfo.sellPrice != -1).findFirst().map(itemPriceEntry -> itemPriceEntry.itemPriceInfo.sellPrice * count).orElse(-1.0);
    }

    public static double getImporterBuyPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(entry -> entry.itemDescription.equals(itemDescription) && entry.itemPriceInfo.importerBuyPrice != -1).findFirst().map(itemPriceEntry -> itemPriceEntry.itemPriceInfo.importerBuyPrice * count).orElse(-1.0);
    }

    // TODO: check for slot/category too (there can be multiple items with the same ItemDescription)
    public static double getAdminShopSellPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(entry -> entry.itemDescription.equals(itemDescription) && entry.itemPriceInfo.adminShopSellPrice != -1).findFirst().map(itemPriceEntry -> itemPriceEntry.itemPriceInfo.adminShopSellPrice * count).orElse(-1.0);
    }

    // TODO: check for slot/category too (there can be multiple items with the same ItemDescription)
    public static double getAdminShopBuyPrice(ItemDescription itemDescription, int count) {
        return itemPriceInfos.stream().filter(entry -> entry.itemDescription.equals(itemDescription) && entry.itemPriceInfo.adminShopBuyPrice != -1).findFirst().map(itemPriceEntry -> itemPriceEntry.itemPriceInfo.adminShopBuyPrice * count).orElse(-1.0);
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

                    ItemPriceInfo priceInfo = ItemPriceInfo.fromJson(object);
                    if (itemDescription != null && priceInfo != null) {
                        itemPriceInfos.add(new ItemPriceEntry(itemDescription, priceInfo));
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
            CompoundTag itemTag = entry.itemDescription.toNbt().copy();
            itemTag.putDouble("sellPrice", entry.itemPriceInfo.sellPrice);
            itemTag.putDouble("importerBuyPrice", entry.itemPriceInfo.importerBuyPrice);
            itemTag.putDouble("adminShopBuyPrice", entry.itemPriceInfo.adminShopBuyPrice);

            if (entry.itemPriceInfo.category != null) {
                itemTag.putString("category", entry.itemPriceInfo.category);
            }
            listTag.add(itemTag);
        });
        return listTag;
    }

    public static CompoundTag toAdminShopCompound(Player player) {
        CompoundTag tag = new CompoundTag();

        ListTag itemsTag = new ListTag();
        ListTag categoriesTag = new ListTag();

        itemPriceInfos.forEach((entry) -> {
            if (entry.itemPriceInfo.adminShopBuyPrice <= 0 && entry.itemPriceInfo.adminShopSellPrice <= 0) {
                return;
            }

            CompoundTag itemTag = entry.itemDescription.toNbt();
            itemTag.putDouble("adminShopBuyPrice", entry.itemPriceInfo.adminShopBuyPrice);
            itemTag.putDouble("adminShopSellPrice", entry.itemPriceInfo.adminShopSellPrice);
            itemTag.putString("category", entry.itemPriceInfo.category);
            itemTag.putInt("slot", entry.itemPriceInfo.adminShopSlot);

            if (entry.itemPriceInfo.customAdminShopName != null) {
                itemTag.putString("customAdminShopName", entry.itemPriceInfo.customAdminShopName);
            }

            if (entry.itemPriceInfo.adminShopStage != null) {
                itemTag.putString("adminShopStage", entry.itemPriceInfo.adminShopStage);
            }

            itemsTag.add(itemTag);
        });

        categories.forEach((category, categories) -> {
            CompoundTag compoundTag = new CompoundTag();

            String itemName = ItemHelper.getItemName(category.icon);

            if (itemName == null) {
                return;
            }

            compoundTag.putString("name", category.name);
            compoundTag.putString("item", itemName);

            ListTag innerCategories = new ListTag();

            categories.forEach(categoryInner -> {
                CompoundTag compoundTagInner = new CompoundTag();

                String itemNameInner = ItemHelper.getItemName(categoryInner.icon);

                if (itemNameInner == null) {
                    return;
                }

                compoundTagInner.putString("name", categoryInner.name);
                compoundTagInner.putString("item", itemNameInner);

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
