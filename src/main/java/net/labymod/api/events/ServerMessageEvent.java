package net.labymod.api.events;

import com.google.gson.JsonElement;

public interface ServerMessageEvent {
    void onServerMessage(String messageKey, JsonElement serverMessage);
}
