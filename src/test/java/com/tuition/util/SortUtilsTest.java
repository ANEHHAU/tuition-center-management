package com.tuition.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortUtilsTest {

    @Test
    void testExtractLastName() {
        assertEquals("An", SortUtils.extractLastName("Nguyễn Văn An"));
        assertEquals("Cường", SortUtils.extractLastName("Lê Minh Cường"));
        assertEquals("An", SortUtils.extractLastName("An"));
        assertEquals("An", SortUtils.extractLastName("  Nguyễn   Văn   An  "));
        assertEquals("", SortUtils.extractLastName("   "));
        assertEquals("", SortUtils.extractLastName(null));
    }

    @Test
    void testSortListByLastName_Ascending() {
        List<String> input = Arrays.asList(
                "Trần Thị Bình",
                "Nguyễn Văn An",
                "Lê Minh Cường",
                "Hoàng Thu Hà",
                "Phạm Đức Dũng"
        );

        List<String> sorted = SortUtils.sortListByLastName(input, Function.identity(), true);

        // Thứ tự mong đợi: An, Bình, Cường, Dũng, Hà
        assertEquals("Nguyễn Văn An", sorted.get(0));
        assertEquals("Trần Thị Bình", sorted.get(1));
        assertEquals("Lê Minh Cường", sorted.get(2));
        assertEquals("Phạm Đức Dũng", sorted.get(3));
        assertEquals("Hoàng Thu Hà", sorted.get(4));
    }

    @Test
    void testSortListByLastName_Descending() {
        List<String> input = Arrays.asList(
                "Trần Thị Bình",
                "Nguyễn Văn An",
                "Lê Minh Cường",
                "Hoàng Thu Hà",
                "Phạm Đức Dũng"
        );

        List<String> sorted = SortUtils.sortListByLastName(input, Function.identity(), false);

        // Thứ tự mong đợi (desc): Hà, Dũng, Cường, Bình, An
        assertEquals("Hoàng Thu Hà", sorted.get(0));
        assertEquals("Phạm Đức Dũng", sorted.get(1));
        assertEquals("Lê Minh Cường", sorted.get(2));
        assertEquals("Trần Thị Bình", sorted.get(3));
        assertEquals("Nguyễn Văn An", sorted.get(4));
    }

    @Test
    void testSortVietnameseCharacters() {
        // Đ > D trong bảng chữ cái tiếng Việt
        List<String> input = Arrays.asList(
                "Đỗ Văn An",
                "Dương Văn Bình",
                "Đỗ Văn Bình",
                "Đỗ Văn Cường"
        );

        List<String> sorted = SortUtils.sortListByLastName(input, Function.identity(), true);

        // An < Bình. "Dương Văn Bình" < "Đỗ Văn Bình".
        assertEquals("Đỗ Văn An", sorted.get(0));
        assertEquals("Dương Văn Bình", sorted.get(1));
        assertEquals("Đỗ Văn Bình", sorted.get(2));
        assertEquals("Đỗ Văn Cường", sorted.get(3));
    }
}
