package net.labymod.user;

import net.labymod.labyconnect.packets.PacketActionPlayResponse;
import net.labymod.user.group.GroupManager;
import net.labymod.utils.Consumer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal compat {@code UserManager} backing the ported LabyConnect code.
 *
 * Holds a per-uuid {@link User} cache and a {@link GroupManager}. Cosmetic-json handling
 * is a no-op for Phase 1 (LabyConnect connection/friends/chat); Phase 4 will bridge
 * {@link #updateUsersJson} into EsdeathForge's cosmetic system. Play-request actions
 * (party invites etc.) resolve to no-ops until that subsystem is ported.
 */
public class UserManager {

    private final Map<UUID, User> users = new ConcurrentHashMap<UUID, User>();
    private final GroupManager groupManager = new GroupManager();

    public User getUser(UUID uuid) {
        User user = users.get(uuid);
        if (user == null) {
            user = new User(uuid);
            users.put(uuid, user);
        }
        return user;
    }

    public void removeCheckedUser(UUID uuid) {
        users.remove(uuid);
        // Socket says this player's cosmetics were cleared -> drop them from the render cache.
        try {
            me.txb1.extras.cosmetics.laby.LabyUserData.invalidate(uuid);
        } catch (Throwable ignored) {
        }
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    /** Clear "familiar" (online LabyMod user) flags on disconnect. */
    public void resetFamiliars() {
        for (User user : users.values()) {
            user.setFamiliar(false);
        }
    }

    public void refresh() {
        // Phase 4: re-fetch cosmetics for tracked users.
    }

    /** Feed the equipped-cosmetic json from the LabyConnect socket into EsdeathForge's cosmetic system. */
    public void updateUsersJson(UUID uuid, String json, Consumer<Boolean> callback) {
        try {
            me.txb1.extras.cosmetics.laby.LabyUserData.feed(uuid, json);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (callback != null) {
            try {
                callback.accept(Boolean.TRUE);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /** Resolve a LabyConnect play-request (party/duel/etc). No-op until LabyPlay is ported. */
    public void resolveAction(short requestId, PacketActionPlayResponse response) {
    }
}
