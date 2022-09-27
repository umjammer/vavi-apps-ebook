/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hmdev.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * JisConverterTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-09-23 nsano initial version <br>
 */
class JisConverterTest {

    @Test
    public void test() throws Exception {
        assertEquals("!", JisConverter.toCharString(0, 0, 1));
        assertEquals("か゚", JisConverter.toCharString(1, 4, 87));
        assertNull(JisConverter.toCharString(1, 12, 90));
        assertEquals("☞", JisConverter.toCharString(1, 13, 94));
        assertEquals("俱", JisConverter.toCharString(1, 14, 1));
        assertEquals("亜", JisConverter.toCharString(1, 16, 1));
        assertEquals("䵷", JisConverter.toCharString(2, 94, 64));
        assertEquals("𪚲", JisConverter.toCharString(2, 94, 86));
    }
}
