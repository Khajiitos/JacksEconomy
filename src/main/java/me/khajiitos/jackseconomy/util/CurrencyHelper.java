package me.khajiitos.jackseconomy.util;

import me.khajiitos.jackseconomy.config.Config;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class CurrencyHelper {
    private static final BigDecimal TRILLION = new BigDecimal("1000000000000");
    private static final BigDecimal BILLION = new BigDecimal("1000000000");
    private static final BigDecimal MILLION = new BigDecimal("1000000");
    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    public static List<ItemStack> getCurrencyItems(BigDecimal value) {
        List<ItemStack> items = new ArrayList<>();
        List<CurrencyType> sortedCurrencies = Config.oneItemCurrencyMode.get() ? List.of(CurrencyType.DOLLAR_BILL) : Arrays.stream(CurrencyType.values()).sorted(Comparator.comparing(CurrencyType::getWorth).reversed()).toList();

        while (value.compareTo(BigDecimal.ZERO) > 0) {
            boolean anything = false;
            for (CurrencyType currencyType : sortedCurrencies) {
                if (value.compareTo(currencyType.worth) >= 0) {
                    int count = Math.min(currencyType.item.getMaxStackSize(), value.divide(currencyType.worth, RoundingMode.DOWN).intValue());
                    items.add(new ItemStack(currencyType.item, count));
                    value = value.subtract(currencyType.worth.multiply(new BigDecimal(count)));
                    anything = true;
                    break;
                }
            }

            if (!anything) {
                break;
            }
        }
        return items;
    }

    // 1.0 -> $1.00
    public static String format(double value) {
        return DecimalFormat.getCurrencyInstance(Locale.US).format(value);
    }

    public static String formatShortened(BigDecimal bigDecimal) {
        String sign = (bigDecimal.compareTo(BigDecimal.ZERO) < 0) ? "-" : "";
        BigDecimal bigDecimalAbs = bigDecimal.abs();

        if (bigDecimalAbs.compareTo(TRILLION) >= 0) {
            return "$" + sign + bigDecimalAbs.divide(TRILLION, RoundingMode.DOWN).setScale(2, RoundingMode.DOWN) + "T";
        } else if (bigDecimalAbs.compareTo(BILLION) >= 0) {
            return "$" + sign + bigDecimalAbs.divide(BILLION, RoundingMode.DOWN).setScale(2, RoundingMode.DOWN) + "B";
        } else if (bigDecimalAbs.compareTo(MILLION) >= 0) {
            return "$" + sign + bigDecimalAbs.divide(MILLION, RoundingMode.DOWN).setScale(2, RoundingMode.DOWN) + "M";
        } else if (bigDecimalAbs.compareTo(THOUSAND) >= 0) {
            return "$" + sign + bigDecimalAbs.divide(THOUSAND, RoundingMode.DOWN).setScale(2, RoundingMode.DOWN) + "K";
        }
        return format(bigDecimal);
    }

    public static String format(BigDecimal bigDecimal) {
        return format(bigDecimal.doubleValue());
    }
}
