package me.khajiitos.jackseconomy.price;

import com.google.gson.JsonObject;

public class ItemPriceInfo {
    public double sellPrice;
    public double importerBuyPrice;
    public double adminShopBuyPrice;
    public String category;
    public int adminShopSlot;
    public String customAdminShopName;

    public ItemPriceInfo(double sellPrice, double importerBuyPrice, double adminShopBuyPrice, String category, int adminShopSlot, String customAdminShopName) {
        this.sellPrice = sellPrice;
        this.importerBuyPrice = importerBuyPrice;
        this.adminShopBuyPrice = adminShopBuyPrice;
        this.category = category;
        this.adminShopSlot = adminShopSlot;
        this.customAdminShopName = customAdminShopName;
    }

    public static ItemPriceInfo fromJson(JsonObject jsonObject) {
        try {
            double sellPrice = jsonObject.has("sellPrice") ? jsonObject.get("sellPrice").getAsDouble() : -1;
            double importerBuyPrice = jsonObject.has("importerBuyPrice") ? jsonObject.get("importerBuyPrice").getAsDouble() : -1;
            double adminShopBuyPrice = jsonObject.has("adminShopBuyPrice") ? jsonObject.get("adminShopBuyPrice").getAsDouble() : -1;
            String category = jsonObject.has("category") ? jsonObject.get("category").getAsString() : null;
            int adminShopSlot = jsonObject.has("adminShopSlot") ? jsonObject.get("adminShopSlot").getAsInt() : -1;
            String customAdminShopName = jsonObject.has("customAdminShopName") ? jsonObject.get("customAdminShopName").getAsString() : null;

            return new ItemPriceInfo(sellPrice, importerBuyPrice, adminShopBuyPrice, category, adminShopSlot, customAdminShopName);
        } catch (NullPointerException | ClassCastException e) {
            return null;
        }
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();

        if (this.sellPrice != -1) {
            jsonObject.addProperty("sellPrice", this.sellPrice);
        }

        if (this.importerBuyPrice != -1) {
            jsonObject.addProperty("importerBuyPrice", this.importerBuyPrice);
        }

        if (this.adminShopBuyPrice != -1) {
            jsonObject.addProperty("adminShopBuyPrice", this.adminShopBuyPrice);
        }

        if (this.category != null) {
            jsonObject.addProperty("category", this.category);
        }

        if (this.adminShopSlot != -1) {
            jsonObject.addProperty("adminShopSlot", this.adminShopSlot);
        }

        if (this.customAdminShopName != null) {
            jsonObject.addProperty("customAdminShopName", this.customAdminShopName);
        }

        return jsonObject;
    }
}