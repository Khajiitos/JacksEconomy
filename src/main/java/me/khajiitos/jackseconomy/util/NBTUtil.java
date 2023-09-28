package me.khajiitos.jackseconomy.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.nbt.*;

public class NBTUtil {

    public static JsonElement nbtToJson(Tag tag) {
        if (tag instanceof StringTag stringTag) {
            return new JsonPrimitive(stringTag.getAsString());
        } else if (tag instanceof IntTag intTag) {
            return new JsonPrimitive(intTag.getAsInt());
        } else if (tag instanceof DoubleTag doubleTag) {
            return new JsonPrimitive(doubleTag.getAsDouble());
        } else if (tag instanceof ShortTag shortTag) {
            return new JsonPrimitive(shortTag.getAsShort());
        } else if (tag instanceof CompoundTag compoundTag) {
            JsonObject resultObj = new JsonObject();

            compoundTag.getAllKeys().forEach(key -> {
                Tag objTag = compoundTag.get(key);

                if (objTag == null) {
                    return;
                }

                resultObj.add(key, nbtToJson(objTag));
            });

            return resultObj;
        } else if (tag instanceof ListTag listTag) {
            JsonArray resultArray = new JsonArray();

            listTag.forEach(arrayTag -> {
                if (arrayTag == null) {
                    return;
                }

                resultArray.add(nbtToJson(arrayTag));
            });

            return resultArray;
        } else {
            JacksEconomy.LOGGER.warn("NBTUtil::nbtToJson - unsupported tag type " + tag.getClass().getSimpleName());
        }

        return null;
    }

    public static Tag jsonToNbt(JsonElement jsonElement) {
        if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
            if (jsonPrimitive.isBoolean()) {
                return ByteTag.valueOf(jsonElement.getAsBoolean());
            } else if (jsonPrimitive.isNumber()) {
                // There is no way to differentiate what type of number that is - double is the most universal
                return DoubleTag.valueOf(jsonPrimitive.getAsDouble());
            } else if (jsonPrimitive.isString()) {
                return StringTag.valueOf(jsonPrimitive.getAsString());
            }
        } else if (jsonElement instanceof JsonObject jsonObject) {
            CompoundTag resultTag = new CompoundTag();
            jsonObject.entrySet().forEach(entry -> {
                if (entry.getValue() != null) {
                    Tag tag = jsonToNbt(entry.getValue());
                    if (tag != null) {
                        resultTag.put(entry.getKey(), tag);
                    }
                }
            });

            return resultTag;
        } else if (jsonElement instanceof JsonArray jsonArray) {
            ListTag listTag = new ListTag();
            jsonArray.forEach(element -> listTag.add(jsonToNbt(element)));
            return listTag;
        } else {
            JacksEconomy.LOGGER.warn("NBTUtil::jsonToNbt - unsupported element type " + jsonElement.getClass().getSimpleName());
        }

        return null;
    }
}
