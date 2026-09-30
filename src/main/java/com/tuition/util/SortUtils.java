package com.tuition.util;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tiện ích hỗ trợ sắp xếp dữ liệu.
 */
public class SortUtils {

    // Sử dụng Collator của Việt Nam để so sánh chữ cái có dấu chính xác (VD: a, ă, â, b, c...)
    private static final Collator VI_COLLATOR = Collator.getInstance(new Locale("vi", "VN"));

    /**
     * Sắp xếp danh sách User (hoặc bất kỳ Entity nào) theo Tên cuối cùng (Tên chính)
     * @param list Danh sách đầu vào
     * @param nameExtractor Hàm lấy ra fullName từ Entity
     * @param asc Sắp xếp tăng dần hay giảm dần
     * @return Danh sách đã sắp xếp
     */
    public static <T> List<T> sortListByLastName(List<T> list, Function<T, String> nameExtractor, boolean asc) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Comparator<T> comparator = (a, b) -> {
            String nameA = nameExtractor.apply(a);
            String nameB = nameExtractor.apply(b);
            
            if (nameA == null) nameA = "";
            if (nameB == null) nameB = "";

            String lastNameA = extractLastName(nameA);
            String lastNameB = extractLastName(nameB);

            // So sánh từ cuối cùng bằng tiếng Việt
            int compare = VI_COLLATOR.compare(lastNameA, lastNameB);
            
            if (compare == 0) {
                // Nếu tên giống nhau thì so sánh toàn bộ fullName
                compare = VI_COLLATOR.compare(nameA.trim(), nameB.trim());
            }

            return asc ? compare : -compare;
        };

        return list.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Tách lấy từ cuối cùng trong họ và tên.
     * VD: "Nguyễn Văn An" -> "An"
     * VD: "Lê Minh Cường" -> "Cường"
     * VD: "  Nguyễn   Văn   An  " -> "An"
     */
    public static String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}
