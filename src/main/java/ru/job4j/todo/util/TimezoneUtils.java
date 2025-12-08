package ru.job4j.todo.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Locale;
import java.util.TimeZone;

public class TimezoneUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Проверяет, является ли строка корректным часовым поясом
     */
    public static boolean isValidTimezone(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        try {
            ZoneId.of(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Форматирует LocalDateTime в строку с учётом часового пояса.
     * Предполагается, что dateTime уже хранится в часовом поясе пользователя.
     */
    public static String formatDateTime(LocalDateTime dateTime, String userTimezone) {
        if (dateTime == null) {
            return "";
        }
        if (userTimezone == null || userTimezone.isEmpty() || !isValidTimezone(userTimezone)) {
            return dateTime.format(FORMATTER);
        }
        try {
            ZoneId zone = ZoneId.of(userTimezone);
            ZonedDateTime zdt = dateTime.atZone(zone);
            return zdt.format(FORMATTER);
        } catch (Exception e) {
            return dateTime.format(FORMATTER);
        }
    }

    /**
     * Возвращает список всех доступных часовых поясов
     */
    public static List<TimeZone> getAllTimezones() {
        return Arrays.stream(TimeZone.getAvailableIDs())
                .map(TimeZone::getTimeZone)
                .sorted(Comparator.comparing(TimeZone::getID))
                .toList();
    }

    /**
     * Получает человекочитаемое имя часового пояса
     */
    public static String getDisplayName(String timezoneId) {
        if (timezoneId == null || timezoneId.isEmpty()) {
            return "По умолчанию (системное время)";
        }
        TimeZone tz = TimeZone.getTimeZone(timezoneId);
        return timezoneId + " (" + tz.getDisplayName(Locale.getDefault()) + ")";
    }

    /**
     * Получает текущее время в указанном часовом поясе
     */
    public static LocalDateTime getCurrentDateTimeInTimezone(String timezoneId) {
        if (timezoneId == null || timezoneId.isEmpty() || !isValidTimezone(timezoneId)) {
            return LocalDateTime.now();
        }
        try {
            ZoneId zone = ZoneId.of(timezoneId);
            return LocalDateTime.now(zone);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}