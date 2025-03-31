package com.on.eye.api.global.common.util;

import java.time.LocalDateTime;

public class LocalDateTimeUtils {
    private LocalDateTimeUtils() {}

    public static LocalDateTime todayStartTime() {
        return getStartOfDay(LocalDateTime.now());
    }

    public static LocalDateTime todayEndTime() {
        return getEndOfDay(LocalDateTime.now());
    }

    public static LocalDateTime getStartOfDay(LocalDateTime dateTimeToUse) {
        return dateTimeToUse.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static LocalDateTime getEndOfDay(LocalDateTime dateTimeToUse) {
        return dateTimeToUse.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }
}
