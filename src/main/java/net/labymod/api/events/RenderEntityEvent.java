package net.labymod.api.events;

import net.minecraft.entity.Entity;

public interface RenderEntityEvent {
    void onRender(Entity entity, double x, double y, double z, float partialTicks);
}
