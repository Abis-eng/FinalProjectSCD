package com.elcinic.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtil {

    private static final NumberFormat PKR = NumberFormat.getNumberInstance(new Locale("en", "PK"));

    static {
        PKR.setMaximumFractionDigits(0);
        PKR.setMinimumFractionDigits(0);
    }

    private MoneyUtil() {
    }

    public static String format(Object amount) {
        if (amount == null) {
            return "Rs. 0";
        }
        BigDecimal value;
        if (amount instanceof BigDecimal bd) {
            value = bd;
        } else if (amount instanceof Number n) {
            value = BigDecimal.valueOf(n.doubleValue());
        } else {
            value = new BigDecimal(amount.toString());
        }
        value = value.setScale(0, RoundingMode.HALF_UP);
        return "Rs. " + PKR.format(value);
    }
}
