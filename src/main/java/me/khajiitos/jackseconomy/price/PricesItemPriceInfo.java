package me.khajiitos.jackseconomy.price;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PricesItemPriceInfo extends ItemPriceInfo {
    public double sellPrice;
    public double adminShopSellPrice;
    public double importerBuyPrice;
    public String adminShopSellStage;

    public PricesItemPriceInfo(double sellPrice, double adminShopSellPrice, double importerBuyPrice, String adminShopSellStage) {
        this.sellPrice = sellPrice;
        this.adminShopSellPrice = adminShopSellPrice;
        this.importerBuyPrice = importerBuyPrice;
        this.adminShopSellStage = adminShopSellStage;
    }

    protected static @Nullable ItemPriceInfo fromJsonOrNull(JsonObject jsonObject) {
        try {
            if (hasAny(jsonObject, List.of("sellPrice", "adminShopSellPrice", "importerBuyPrice"))) {

                double sellPrice = jsonObject.has("sellPrice") ? jsonObject.get("sellPrice").getAsDouble() : -1;
                double adminShopSellPrice = jsonObject.has("adminShopSellPrice") ? jsonObject.get("adminShopSellPrice").getAsDouble() : -1;
                double importerBuyPrice = jsonObject.has("importerBuyPrice") ? jsonObject.get("importerBuyPrice").getAsDouble() : -1;
                String adminShopSellStage = jsonObject.has("adminShopSellStage") ? jsonObject.get("adminShopSellStage").getAsString() : null;

                return new PricesItemPriceInfo(sellPrice, adminShopSellPrice, importerBuyPrice, adminShopSellStage);
            }

        } catch (NullPointerException | ClassCastException ignored) {}
        return null;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "prices");

        if (this.adminShopSellPrice != -1) {
            jsonObject.addProperty("adminShopSellPrice", this.adminShopSellPrice);
        }

        if (this.sellPrice != -1) {
            jsonObject.addProperty("sellPrice", this.sellPrice);
        }

        if (this.importerBuyPrice != -1) {
            jsonObject.addProperty("importerBuyPrice", this.importerBuyPrice);
        }

        if (this.adminShopSellStage != null) {
            jsonObject.addProperty("adminShopSellStage", this.adminShopSellStage);
        }

        return jsonObject;
    }
}
