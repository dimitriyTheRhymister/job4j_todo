package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.todo.model.User;
import ru.job4j.todo.repository.UserRepository;
import ru.job4j.todo.util.TimezoneUtils;

import java.util.*;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> save(User user) {
        // Убедимся, что часовой пояс установлен
        if (user.getTimezone() == null || user.getTimezone().isEmpty()) {
            user.setTimezone("Europe/Moscow");
        }
        return Optional.ofNullable(userRepository.createUser(user));
    }

    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByLoginAndPassword(String login, String password) {
        return userRepository.findByLoginAndPassword(login, password);
    }

    public boolean update(User user) {
        return userRepository.updateUser(user);
    }

    public boolean deleteById(int id) {
        return userRepository.deleteById(id);
    }

    public Map<String, String> getAllTimezones() {
        List<TimeZone> zones = TimezoneUtils.getAllTimezones();
        Map<String, String> timezoneMap = new LinkedHashMap<>();

        timezoneMap.put("Europe/Moscow", TimezoneUtils.getDisplayName("Europe/Moscow"));
        timezoneMap.put("Europe/London", TimezoneUtils.getDisplayName("Europe/London"));
        timezoneMap.put("America/New_York", TimezoneUtils.getDisplayName("America/New_York"));
        timezoneMap.put("Asia/Tokyo", TimezoneUtils.getDisplayName("Asia/Tokyo"));
        timezoneMap.put("Australia/Sydney", TimezoneUtils.getDisplayName("Australia/Sydney"));
        timezoneMap.put("", "-- Выберите часовой пояс --");

        for (TimeZone zone : zones) {
            String id = zone.getID();
            if (!timezoneMap.containsKey(id)) {
                timezoneMap.put(id, TimezoneUtils.getDisplayName(id));
            }
        }
        return timezoneMap;
    }

    public Map<String, String> getPopularTimezones() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("", "-- Выберите часовой пояс --");
        map.put("Europe/Moscow", TimezoneUtils.getDisplayName("Europe/Moscow"));
        map.put("Europe/Kiev", TimezoneUtils.getDisplayName("Europe/Kiev"));
        map.put("Europe/London", TimezoneUtils.getDisplayName("Europe/London"));
        map.put("Europe/Paris", TimezoneUtils.getDisplayName("Europe/Paris"));
        map.put("Europe/Berlin", TimezoneUtils.getDisplayName("Europe/Berlin"));
        map.put("America/New_York", TimezoneUtils.getDisplayName("America/New_York"));
        map.put("America/Los_Angeles", TimezoneUtils.getDisplayName("America/Los_Angeles"));
        map.put("Asia/Tokyo", TimezoneUtils.getDisplayName("Asia/Tokyo"));
        map.put("Asia/Shanghai", TimezoneUtils.getDisplayName("Asia/Shanghai"));
        map.put("Australia/Sydney", TimezoneUtils.getDisplayName("Australia/Sydney"));
        return map;
    }
}