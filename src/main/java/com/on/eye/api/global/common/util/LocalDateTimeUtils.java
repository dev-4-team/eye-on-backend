package com.on.eye.api.global.common.util;

import java.time.LocalDateTime;

public class LocalDateTimeUtils {
    private LocalDateTimeUtils() {}

    public static LocalDateTime todayStartTime() {
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
    }
}
