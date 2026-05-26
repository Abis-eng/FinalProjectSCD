package com.elcinic.utility;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyUtilTest {

    @Test
    void format_pkr() {
        assertEquals("Rs. 3,000", MoneyUtil.format(3000));
        assertEquals("Rs. 1,500", MoneyUtil.format(BigDecimal.valueOf(1500.4)));
    }
}
