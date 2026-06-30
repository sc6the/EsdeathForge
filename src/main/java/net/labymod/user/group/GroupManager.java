package net.labymod.user.group;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Minimal compat group registry. Lazily creates a {@link LabyGroup} per id so
 * LabyConnect's badge packet ({@code PacketUserBadge}) can tag users by rank.
 */
public class GroupManager {

    private final Map<Short, LabyGroup> groups = new ConcurrentHashMap<Short, LabyGroup>();

    public LabyGroup getGroupById(short id) {
        LabyGroup group = groups.get(id);
        if (group == null) {
            group = new LabyGroup(id, "group_" + id);
            groups.put(id, group);
        }
        return group;
    }
}
