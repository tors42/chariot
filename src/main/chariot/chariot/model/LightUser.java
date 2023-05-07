package chariot.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import chariot.model.UserData.UserPropertyEnum;

public record LightUser (UserData _userData) implements UserCommon {
    public static LightUser of(String id, String name, String title, boolean patron) {
        Map<UserPropertyEnum, Object> map = new HashMap<>();
        map.put(UserPropertyEnum.id, id);
        map.put(UserPropertyEnum.name, name);
        if (title != null && !title.isEmpty()) map.put(UserPropertyEnum.title, title);
        if (patron) map.put(UserPropertyEnum.patron, patron);
        return new LightUser(new UserData(new EnumMap<>(map)));
    }
}
