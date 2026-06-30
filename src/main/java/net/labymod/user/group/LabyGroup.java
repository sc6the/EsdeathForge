package net.labymod.user.group;

/**
 * Minimal compat group/rank holder. The original carried colour/prefix/badge metadata;
 * LabyConnect only sets it on users by id. Extend when wiring badge rendering.
 */
public class LabyGroup {

    private final short id;
    private final String name;

    public LabyGroup(short id, String name) {
        this.id = id;
        this.name = name;
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
